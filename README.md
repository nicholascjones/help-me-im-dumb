# help-me-im-dumb

Search engine for eli5 and various other soruces

## Installation

Install [Leiningen](https://leiningen.org/)

## Usage
The indices must be created from a jsonfile (created from `reddit-scraper.py`)
before the search engine can run. After being created once, the server can be run.

    $ lein run -index jsonfile.jsonl # runs index on line separated json file
    $ lein run -server 5000 # starts web server on port 5000

## Why Clojure?
In order to allow help-me-im-dumb to effectively search through the reddit posts, an index must be made for the search. The effectiveness of the search is increased with a better index. When it comes to Clojure's index creation, Clojure does it concurrently to help speed up the process. The functions that are made within Clojure are also more pure in their ability to run..
