# Reproduction package for an entropy computation program for relational database schemas
# Contains experiments with scripts for execution
#
# Copyright 2024, Christoph Köhnen <christoph.koehnen@uni-passau.de>
# SPDX-License-Identifier: MIT

FROM ubuntu:22.04

MAINTAINER Christoph Köhnen <christoph.koehnen@uni-passau.de>

ENV DEBIAN_FRONTEND noninteractive
ENV LANG="C.UTF-8"
ENV LC_ALL="C.UTF-8"

# Install packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    git \
    maven \
    openjdk-18-jdk \
    openjdk-18-jre \
    python3 \
    python3-pip

# Install dev packages
RUN apt-get install -y --no-install-recommends \
    nano \
    sudo

# Add user
RUN useradd -m -G sudo -s /bin/bash repro && echo "repro:repro" | chpasswd
RUN usermod -a -G staff repro
USER repro
WORKDIR /home/repro

# Add content to container
ADD --chown=repro:repro experiments /home/repro/experiments

# Install python packages
RUN pip install seaborn

# Copy and install entropy computation program
ADD --chown=repro:repro schema-entropy /home/repro/schema-entropy
RUN cd schema-entropy && ./setup.sh
RUN cp schema-entropy/schema-entropy.jar experiments
