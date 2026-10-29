ARG BASE_CONTAINER=quay.io/jupyter/all-spark-notebook:spark-4.2.1
ARG BUILDER_CONTAINER=sbtscala/scala-sbt:eclipse-temurin-21.0.12.1_1_1.13.1_2.13.18

FROM $BUILDER_CONTAINER AS builder

WORKDIR /coinbase-spark

COPY build.sbt .
COPY project project

RUN sbt update #prefetch dependencies & sbt

COPY src src

RUN sbt clean assembly

FROM $BASE_CONTAINER

COPY --from=builder /coinbase-spark/target/scala-2.13/coinbase-spark-assembly-0.1.0-SNAPSHOT.jar /usr/local/spark/jars
COPY Notebook.ipynb work/
