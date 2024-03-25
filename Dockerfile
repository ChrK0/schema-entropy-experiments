# Reproduction package for an entropy computation program for relational database schemas
# Contains experiments with scripts for execution
#
# Copyright 2024, Christoph Köhnen <christoph.koehnen@uni-passau.de>
# Copyright 2024, Stefan Klessinger <stefan.klessinger@uni-passau.de>
# SPDX-License-Identifier: GPL-3.0

FROM maven as build

WORKDIR /home
COPY schema-entropy ./schema-entropy
RUN cd schema-entropy && ./setup.sh

FROM ubuntu:22.04

LABEL org.opencontainers.image.authors="Christoph Köhnen <christoph.koehnen@uni-passau.de>"

ENV DEBIAN_FRONTEND=noninteractive
ENV LANG="C.UTF-8"
ENV LC_ALL="C.UTF-8"

# Install packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    openjdk-18-jre \
    python3 \
    python3-pip \
    sudo

# Add user
RUN useradd -m -G sudo -s /bin/bash repro && echo "repro:repro" | chpasswd
RUN usermod -a -G staff repro
USER repro
WORKDIR /home/repro

# Add experiment scripts and data to container
ADD --chown=repro:repro experiments /home/repro/experiments

# Install python packages
RUN pip install seaborn

# Copy jar of entropy computation program
COPY --chown=repro:repro --from=build /home/schema-entropy/schema-entropy.jar experiments
