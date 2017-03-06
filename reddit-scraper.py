#!/usr/bin/python2
# reddit-scraper.py

import datetime
import json
import praw
import os

SUBREDDIT = 'explainlikeimfive'
DAYS_SINCE_NOW = 1


def get_reddit_instance():
    reddit = praw.Reddit(
        client_id='OF28wOoefaWK9Q',
        client_secret='vGVsC-5CkFI_1d9qkcAu7hVlU58',
        user_agent='linux:myredditapp:v1.0 (by /u/austrinus)',
    )
    return reddit


def get_starting_timestamp(days):
    starting_timestamp = (datetime.datetime.now() - datetime.timedelta(
        days=DAYS_SINCE_NOW,
    )).strftime("%s")
    return starting_timestamp


def get_current_posts_from_file(filename):
    post_ids = set()
    if os.path.exists(filename):
        with open(filename) as f:
            for line in f:
                post = json.loads(line)
                post_ids.add(post['post_id'])
    return post_ids


def get_comments(submission, limit=5):
    comments = []
    for c in submission.comments:
        if len(comments) > limit:
            break
        author = None
        if c.author:
            author = c.author.name
        comment = {
            'author': author,
            'text': c.body,
        }
        comments.append(comment)

    return comments


def save_past_posts_to_file(filename=None, days=10):
    if not filename:
        raise RuntimeError("No filename given")

    reddit = get_reddit_instance()
    starting_timestamp = get_starting_timestamp(days)
    current_posts = get_current_posts_from_file(filename)

    with open(filename, 'a') as f:
        for submission in reddit.subreddit(SUBREDDIT).submissions(
                start=starting_timestamp
        ):
            if submission.id in current_posts:
                continue
            comments = get_comments(submission, limit=10)
            post = {
                'date': datetime.datetime.fromtimestamp(
                    submission.created
                ).strftime('%c'),
                'title': submission.title,
                'body': submission.selftext,
                'post_id': submission.id,
                'comments': comments,
            }
            print post
            f.write(json.dumps(post) + '\n')

save_past_posts_to_file('posts.jsonl', 1)
