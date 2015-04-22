#!/usr/bin/env python
# coding=utf-8

__author__ = 'Souleiman Ayoub'

import os
import json
import codecs

from os import listdir
from sys import argv

# argv := merge_all.py [path_to_alda_dir] [path_to_ner_attrib_dir] [output_dir] ([classifier_dir] [classifier_indentifier])

if len(argv) < 4:
    print('Invalid Parameters!')
    print('merge_all.py [path_to_alda_dir] [path_to_ner_attrib_dir] [output_dir]')
    exit(1)

if __name__ == '__main__':
    alda_files = [file for file in listdir(argv[1]) if file.endswith('alda.json')]
    ner_files = [file for file in listdir(argv[2]) if file.endswith('attribute_ner.json')]

    if len(alda_files) != len(ner_files):
        print('Count mismatch between ALDA and NER files')
        exit(1)

    for i in range(0, len(alda_files)):
        alda_file = alda_files[i]
        ner_file = alda_file.replace('alda', 'attribute_ner')
        with codecs.open(os.path.join(argv[1], alda_file), 'r', encoding='UTF-8') as a, \
                codecs.open(os.path.join(argv[2], ner_file), 'r', encoding='UTF-8') as n:
            alda = json.loads(a.read(), encoding='UTF-8')
            ner = json.loads(n.read(), encoding='UTF-8')

            if len(argv) == 6:
                cls_file = alda_file.replace('alda', argv[5])
                with codecs.open(os.path.join(argv[4], cls_file), 'r', encoding='UTF-8') as c:
                    ner['category'] = json.loads(c.read(), encoding='UTF-8')

        both = ner.copy()
        both["Topics"] = alda
        filename = alda_file.replace('alda.json', 'final.json')
        with codecs.open(os.path.join(argv[3], filename), 'w', encoding='utf-8') as w:
            w.write(json.dumps(both, ensure_ascii=False, indent=2))
