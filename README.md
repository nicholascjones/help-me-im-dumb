# help-me-im-dumb

Search engine for eli5 and various other soruces

## Installation

Install [Leiningen](https://leiningen.org/)

## Usage
The indices must be created from a jsonfile (created from `reddit-scraper.py`)
before the search engine can run. After being created once, the server can be run.

    $ lein run -index jsonfile.jsonl # runs index on line separated json file
    $ lein run -server 5000 # starts web server on port 5000
