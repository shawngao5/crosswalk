#!/usr/bin/env python

# Copyright (c) 2015 Intel Corporation. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.

import argparse
import os
import sys
import shutil
import tempfile

from util import RunCommand
from webp_util import ConvertJpeg2WebPUnderPath


def main():
  parser = argparse.ArgumentParser(description='Convert jpeg images to webp.')
  parser.add_argument('-p', '--path', type=str, default=os.getcwd(), \
                      help='convert all jpeg images under this path.')
  parser.add_argument('-q', '--quality', type=int, default=80, \
                      help='set output quality, \
                      range [0, 100].')
  parser.add_argument('-aq', '--alpha-quality', dest='alpha_quality', \
                      type=int, default=100, \
                      help='set output quality of alpha channel, \
                      range [0, 100].')
  args = parser.parse_args()

  ConvertJpeg2WebPUnderPath(args.path, args.quality, args.alpha_quality)


if __name__ == '__main__':
  sys.exit(main())
