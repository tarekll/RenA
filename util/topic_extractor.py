#!/usr/bin/env python
# coding=utf-8
__author__ = 'Souleiman Ayoub'

import os
import codecs
import json

from os import listdir
from sys import argv

# argv := topic_extractor.py [input_dir] [output_dir]

if len(argv) != 3:
    print('Invalid Parameters!')
    print('topic_extractor.py [input_dir] [output_dir]')
    exit(1)

if __name__ == '__main__':
    files = [file for file in listdir(argv[1]) if file.endswith('.csv')]

    for f in files:
        with codecs.open(os.path.join(argv[1], f), 'r', encoding='utf-8') as r:
            contents = r.read().strip().split("\n")

        header_count = int(len([x for x in contents[0].strip().split(",") if x]) / 2)
        topics = [[] for x in range(header_count)]
        for c in contents[1:]:  # Skip header
            values = [x for x in c.strip().split(",") if x]  # Filter out empty strings in list

            num_topics = int(len(values) / 2)
            for i in range(0, num_topics):
                topics[i].append(values[i * 2])
        topic_model = {}
        for i in range(0, header_count):
            topic_model['Topic {}'.format(i + 1)] = topics[i]

        output = json.dumps(topic_model, ensure_ascii=False, indent=2)
        with codecs.open(os.path.join(argv[2], f).replace('.csv', '_alda.json'), 'w', encoding='UTF-8') as w:
            w.write(output)
