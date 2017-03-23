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
In order to allow help-me-im-dumb to effectively search through the reddit posts, an index must be made for the search. The effectiveness of the search is increased with a better index. When it comes to Clojure's index creation, Clojure does it concurrently to help speed up the process.

Clojure's handling of variables make sure that no side effects will occur for unintended variables when functions run. Clojure is very explicit in controlling which variables can be modified. This control is necessary for the index creation and to get the results from the search to the html page.
