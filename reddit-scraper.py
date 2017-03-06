#!/usr/bin/python2
# reddit-scraper.py

import argparse
import datetime
import json
import praw
import os

SUBREDDIT = 'explainlikeimfive'
MAX_COMMENTS = 10


def get_reddit_instance():
    reddit = praw.Reddit(
        client_id='OF28wOoefaWK9Q',
        client_secret='vGVsC-5CkFI_1d9qkcAu7hVlU58',
        user_agent='linux:myredditapp:v1.0 (by /u/austrinus)',
    )
    return reddit


def get_starting_timestamp(days):
    starting_timestamp = (datetime.datetime.now() - datetime.timedelta(
        days=days,
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


def get_comments(submission, limit=MAX_COMMENTS):
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
            comments = get_comments(submission, limit=MAX_COMMENTS)
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


def parse_args():
    parser = argparse.ArgumentParser(
        description='Scraps reddit data into file'
    )
    parser.add_argument(
        'file',
        help='file to save data',
    )
    parser.add_argument(
        '--subreddit',
        type=str,
        help='subreddit to scrape',
        required=True
    )
    parser.add_argument(
        '--days',
        type=int,
        help='how many days in past to search',
        required=True,
    )
    parser.add_argument(
        '--max-comments',
        default=10,
        help='# of comments to save per post',
        type=int,
    )

    return parser.parse_args()


def main():
    global SUBREDDIT
    global MAX_COMMENTS
    args = parse_args()
    SUBREDDIT = args.subreddit
    days = args.days
    f = args.file
    MAX_COMMENTS = args.max_comments
    save_past_posts_to_file(f, days)


main()
