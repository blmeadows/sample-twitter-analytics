# Sample Twitter Analytics

Collects various statistics of the sample Twitter stream.

To run
------

Set the following environment variables:

```
TWITTER_CONSUMER_TOKEN_KEY

TWITTER_CONSUMER_TOKEN_SECRET

TWITTER_ACCESS_TOKEN_KEY

TWITTER_ACCESS_TOKEN_SECRET
```

Then:
`sbt run`

Statistics are available via `curl http://localhost:8080/statistics`
