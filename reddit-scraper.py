#!/usr/bin/python2
# reddit-scraper.py

import argparse
import datetime
import json
import logging
import os
import praw

SUBREDDIT = 'explainlikeimfive'
MAX_COMMENTS = 10


def get_reddit_instance():
    reddit = praw.Reddit(
        client_id='OF28wOoefaWK9Q',
        client_secret='vGVsC-5CkFI_1d9qkcAu7hVlU58',
        user_agent='linux:myredditapp:v1.0 (by /u/austrinus)',
    )
    return reddit


def get_timestamp_for_days_from_now(days):
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


def save_past_posts_to_file(filename=None, start_days=10, end_days=None):
    if not filename:
        raise RuntimeError("No filename given")

    reddit = get_reddit_instance()
    starting_timestamp = get_timestamp_for_days_from_now(start_days)
    ending_timestamp = get_timestamp_for_days_from_now(end_days)
    current_posts = get_current_posts_from_file(filename)

    with open(filename, 'a') as f:
        for submission in reddit.subreddit(SUBREDDIT).submissions(
                start=starting_timestamp,
                end=ending_timestamp,
        ):
            if submission.id in current_posts:
                continue
            comments = get_comments(submission, limit=MAX_COMMENTS)
            date = datetime.datetime.fromtimestamp(
                submission.created
            ).strftime('%c')
            title = submission.title
            post = {
                'date': date,
                'title': title,
                'body': submission.selftext,
                'post_id': submission.id,
                'comments': comments,
                'subreddit': submission.subreddit.name,
                'url': submission.url,
            }
            logging.info("title: {}; date: {}".format(
                    title.encode('utf-8'),
                    date
                )
            )
            logging.debug(post)
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
        '--start-days',
        type=int,
        help='how many days in past to begin',
        required=True,
    )
    parser.add_argument(
        '--end-days',
        type=int,
        help='how many days in past to end',
        required=False,
    )
    parser.add_argument(
        '--max-comments',
        default=10,
        help='# of comments to save per post',
        type=int,
    )
    parser.add_argument(
        '-v',
        action='store_true',
        help='INFO debugging',
    )

    return parser.parse_args()


def main():
    global SUBREDDIT
    global MAX_COMMENTS
    logging.basicConfig(level=logging.INFO)
    args = parse_args()
    if args.v:
        logging.basicConfig(level=logging.DEBUG)
    SUBREDDIT = args.subreddit
    start_days = args.start_days
    end_days = args.end_days
    f = args.file
    MAX_COMMENTS = args.max_comments
    save_past_posts_to_file(f, start_days=start_days, end_days=end_days)


main()
