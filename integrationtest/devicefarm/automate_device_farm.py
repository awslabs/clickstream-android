import datetime
import json
import os
import random
import re
import string
import time

import boto3
import requests

# The following script runs a test through Device Farm
client = boto3.client('devicefarm')


def get_config(app_file_path, test_package, project_arn, test_spec_arn, pool_arn):
    return {
        # This is our app under test.
        "appFilePath": app_file_path,
        "projectArn": project_arn,
        # Since we care about the most popular devices, we'll use a curated pool.
        "testSpecArn": test_spec_arn,
        "poolArn": pool_arn,
        "namePrefix": "MyAndroidAppTest",
        # This is our test package. This tutorial won't go into how to make these.
        "testPackage": test_package
    }


def upload_and_test_android(app_file_path, test_package, project_arn, test_spec_arn, pool_arn):
    config = get_config(app_file_path, test_package, project_arn, test_spec_arn, pool_arn)
    print(config)
    unique = config['namePrefix'] + "-" + (datetime.date.today().isoformat()) + (
        ''.join(random.sample(string.ascii_letters, 8)))
    print(f"The unique identifier for this run is going to be {unique} -- all uploads will be prefixed with this.")

    our_upload_arn = upload_df_file(config, unique, config['appFilePath'], "ANDROID_APP")
    our_test_package_arn = upload_df_file(config, unique, 'APPIUM_PYTHON_TEST_PACKAGE')
    print(our_upload_arn, our_test_package_arn)
    # Now that we have those out of the way, we can start the test run...
    response = client.schedule_run(
        projectArn=config["projectArn"],
        appArn=our_upload_arn,
        devicePoolArn=config["poolArn"],
        name=unique,
        test={
            "type": "APPIUM_PYTHON",
            "testSpecArn": config["testSpecArn"],
            "testPackageArn": our_test_package_arn
        }
    )
    run_arn = response['run']['arn']
    start_time = datetime.datetime.now()
    print(f"Run {unique} is scheduled as arn {run_arn} ")

    try:
        while True:
            response = client.get_run(arn=run_arn)
            state = response['run']['status']
            if state == 'COMPLETED' or state == 'ERRORED':
                break
            else:
                print(f" Run {unique} in state {state}, total time " + str(datetime.datetime.now() - start_time))
                time.sleep(10)
    except Exception as e:
        # If something goes wrong in this process, we stop the run and exit.
        print(e)
        client.stop_run(arn=run_arn)
        exit(1)
    print(f"Tests finished in state {state} after " + str(datetime.datetime.now() - start_time))
    # now, we pull all the logs.
    jobs_response = client.list_jobs(arn=run_arn)
    # Save the output somewhere. We're using the unique value, but you could use something else
    save_path = os.path.join(os.getcwd(), unique)
    os.mkdir(save_path)
    # Save the last run information
    logcat_paths = download_artifacts(jobs_response, save_path)
    # done
    print("Finished")
    verify_logcat(logcat_paths)


def upload_df_file(config, unique, type_, mime='application/octet-stream'):
    filename = config['appFilePath']
    response = client.create_upload(projectArn=config['projectArn'],
                                    name=unique + "_" + os.path.basename(filename),
                                    type=type_,
                                    contentType=mime
                                    )
    # Get the upload ARN, which we'll return later.
    upload_arn = response['upload']['arn']
    # We're going to extract the URL of the upload and use Requests to upload it
    upload_url = response['upload']['url']
    with open(filename, 'rb') as file_stream:
        print(f"Uploading {filename} to Device Farm as {response['upload']['name']}... ", end='')
        put_req = requests.put(upload_url, data=file_stream, headers={"content-type": mime})
        print(' done')
        if not put_req.ok:
            raise Exception("Couldn't upload, requests said we're not ok. Requests says: " + put_req.reason)
    started = datetime.datetime.now()
    while True:
        print(f"Upload of {filename} in state {response['upload']['status']} after " + str(
            datetime.datetime.now() - started))
        if response['upload']['status'] == 'FAILED':
            raise Exception("The upload failed processing. DeviceFarm says reason is: \n" + (
                response['upload']['message'] if 'message' in response['upload'] else response['upload']['metadata']))
        if response['upload']['status'] == 'SUCCEEDED':
            break
        time.sleep(5)
        response = client.get_upload(arn=upload_arn)
    print("")
    return upload_arn


def download_artifacts(jobs_response, save_path):
    logcat_paths = []
    for job in jobs_response['jobs']:
        # Make a directory for our information
        job_name = job['name']
        os.makedirs(os.path.join(save_path, job_name), exist_ok=True)
        # Get each suite within the job
        suites = client.list_suites(arn=job['arn'])['suites']
        for suite in suites:
            if suite['name'] == 'Tests Suite':
                for test in client.list_tests(arn=suite['arn'])['tests']:
                    # Get the artifacts
                    for artifact_type in ['FILE', 'SCREENSHOT', 'LOG']:
                        artifacts = client.list_artifacts(
                            type=artifact_type,
                            arn=test['arn']
                        )['artifacts']
                        for artifact in artifacts:
                            # We replace : because it has a special meaning in Windows & macos
                            path_to = os.path.join(save_path, job_name)
                            os.makedirs(path_to, exist_ok=True)
                            filename = artifact['type'] + "_" + artifact['name'] + "." + artifact['extension']
                            if str(filename).endswith(".logcat"):
                                artifact_save_path = os.path.join(path_to, filename)
                                logcat_paths.append(artifact_save_path)
                                print("Downloading " + artifact_save_path)
                                with open(artifact_save_path, 'wb') as fn, \
                                        requests.get(artifact['url'], allow_redirects=True) as request:
                                    fn.write(request.content)
    return logcat_paths


def get_submitted_events(path):
    submitted_events = []
    with open(path, 'r') as file:
        pattern = re.compile(r'Submitted (\d+) events')
        for line in file:
            match = pattern.search(line)
            if match:
                submitted_events.append(int(match.group(1)))
    return submitted_events


def get_recorded_events(path):
    with open(path, 'r') as file:
        log_lines = file.readlines()
    events = []
    # 定义正则表达式模式
    event_pattern = re.compile(r'save event: (\w+) success, event json:(.*)$')
    json_pattern = re.compile(r'(?<=EventRecorder:)(.*)$')

    current_event_name = ''
    current_event_json = ''

    for line in log_lines:
        if current_event_name == '':
            event_match = event_pattern.search(line)
            if event_match:
                current_event_name, _ = event_match.groups()
            else:
                continue
        else:
            json_match = json_pattern.search(line)
            if json_match:
                current_event_json += json_match.group()
                if json_match.group() == ' }':
                    events.append({
                        'event_name': current_event_name,
                        'event_json': json.loads(current_event_json)
                    })
                    current_event_name = ''
                    current_event_json = ''
            else:
                continue
    return events


def verify_logcat(logcat_paths):
    for path in logcat_paths:
        print("Start verify: " + path)
        submitted_events = get_submitted_events(path)
        recorded_events = get_recorded_events(path)
        # assert all record events are submitted.
        assert sum(submitted_events) == len(recorded_events)

        # assert launch events
        assert recorded_events[0]['event_name'] == '_first_open'
        assert recorded_events[1]['event_name'] == '_app_start'
        assert recorded_events[2]['event_name'] == '_session_start'

        # assert _screen_view
        assert recorded_events[3]['event_name'] == '_screen_view'
        screen_view_event = next((event for event in recorded_events if '_screen_view' in event.get('event_name', '')),
                                 None)
        assert screen_view_event['event_json']['attributes']['_entrances'] == 1
        assert '_screen_id' in screen_view_event['event_json']['attributes']
        assert '_screen_name' in screen_view_event['event_json']['attributes']
        assert '_screen_unique_id' in screen_view_event['event_json']['attributes']

        assert '_session_id' in screen_view_event['event_json']['attributes']
        assert '_session_start_timestamp' in screen_view_event['event_json']['attributes']
        assert '_session_duration' in screen_view_event['event_json']['attributes']
        assert '_session_number' in screen_view_event['event_json']['attributes']

        # assert _profile_set
        profile_set_event = [event for event in recorded_events if '_profile_set' in event.get('event_name', '')]
        assert '_user_id' not in profile_set_event[-1]['event_json']['user']
        assert '_user_id' in profile_set_event[-2]['event_json']['user']

        # assert _user_engagement
        user_engagement_event = next(
            (event for event in recorded_events if '_user_engagement' in event.get('event_name', '')),
            None)
        assert '_engagement_time_msec' in user_engagement_event['event_json']['attributes']
        assert user_engagement_event['event_json']['attributes']['_engagement_time_msec'] > 1000

        # assert _app_end
        assert recorded_events[-1]['event_name'] == '_app_end'
        print("Logcat verify success")