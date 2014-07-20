Listening to the Flock
======================

This repository contains a selection of the code developed in the context of my master's thesis. The goal of the thesis was the application of graph classification in order to determine the political climate of the social circles of Twitter users.

To accomplish this we crawl Twitter, a microblogging service, and retrieve connections and interactions between users. We then induce a weighted, directed graph structure from this data. Markov random walks are used in order to obtain a probability distribution over political parties, effectively modelling the distance between users and political parties. We implement the [Adsorption](http://www.esprockets.com/papers/adsorption-yt.pdf) algorithm by Baluja et al. (2008) in the MapReduce paradigm using Apache Crunch. Further we investigate the importance of certain features on the microblogging platform with respect to our problem domain. We find that retweets on Twitter are a valuable indicator of like-mindedness. Reciprocal connections, however, do not seem to exhibit this property.

This repository contains the implementation of the Adsorption algorithm in the [Apache Crunch](https://crunch.apache.org/) framework. The Apache Crunch Java library provides a framework for writing, testing, and running MapReduce pipelines. Its goal is to make pipelines that are composed of many user-defined functions simple to write, easy to test, and efficient to run.

Other parts of the thesis code not included in this repository includes tools for crawling social networks and the induction of the graph structure used as algorithm input. These, in addition to the full thesis text, are available upon request.

There are also some tests included in the repository which act as usage examples. These can be found in `src/test/java` and can be ran by executing `mvn test` (requires Maven 3.0+).

-- Christophe