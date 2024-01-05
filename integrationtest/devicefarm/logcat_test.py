import json
import re

import pytest
import yaml


class TestLogcat:
    path = yaml.safe_load(open("path.yaml", "r"))

    @pytest.mark.parametrize("path", path)
    def test_logcat(self, path):
        print("Start verify: " + str(path))
        submitted_events = get_submitted_events(path)
        recorded_events = get_recorded_events(path)
        # assert all record events are submitted.
        assert sum(submitted_events) == len(recorded_events)
        print("Verifying successful upload of all events.")

        # assert launch events
        assert recorded_events[0]['event_name'] == '_first_open'
        assert recorded_events[1]['event_name'] == '_app_start'
        assert recorded_events[2]['event_name'] == '_session_start'
        print("Verifying successful order of launch events.")

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
        print("Verifying successful attributes of all _screen_view events.")

        # assert _profile_set
        profile_set_event = [event for event in recorded_events if '_profile_set' in event.get('event_name', '')]
        assert '_user_id' not in profile_set_event[-1]['event_json']['user']
        assert '_user_id' in profile_set_event[-2]['event_json']['user']
        print("Verifying successful attributes of _profile_set events.")

        # assert _user_engagement
        user_engagement_event = next(
            (event for event in recorded_events if '_user_engagement' in event.get('event_name', '')),
            None)
        assert '_engagement_time_msec' in user_engagement_event['event_json']['attributes']
        assert user_engagement_event['event_json']['attributes']['_engagement_time_msec'] > 1000
        print("Verifying successful attributes of _user_engagement events.")

        # assert _app_end
        assert recorded_events[-1]['event_name'] == '_app_end'
        print("Verifying successful completion of _app_end event.")
        print("All logcat verification are successful.")


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


if __name__ == '__main__':
    TestLogcat.test_logcat()