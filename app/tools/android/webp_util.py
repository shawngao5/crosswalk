#!/usr/bin/env python

# Copyright (c) 2015 Intel Corporation. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.

import os

from util import RunCommand


cwebp_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'cwebp')


def CheckFileType(file_path, mime_type):
  file_mime_type = RunCommand(['file', '--mime-type', '-b', file_path])
  return file_mime_type.strip() == mime_type.strip()


def ConvertJpeg2WebPUnderPath(root_dir, quality, alpha_quality):
  jpeg_list = GetFileListByMimeType(root_dir, 'image/jpeg')
  for jpeg_path in jpeg_list:
    file_dir = os.path.dirname(jpeg_path)
    jpeg_file_name = os.path.basename(jpeg_path)
    png_file_name = os.path.splitext(jpeg_file_name)[0] + '.png'
    png_path = os.path.join(file_dir, png_file_name)
    RunCommand(['convert', jpeg_path, png_path])
    ConvertPng2LossyWebP(png_path, jpeg_path, quality, alpha_quality)
    os.remove(png_path)


def ConvertPng2LosslessWebP(src_path, dest_path):
  RunCommand([cwebp_path, src_path, '-lossless', '-o', dest_path])


def ConvertPng2LossyWebP(src_path, dest_path, quality, alpha_quality):
  RunCommand([cwebp_path, src_path, \
              '-q', str(quality), \
              '-alpha_q', str(alpha_quality), \
              '-o', dest_path])


def ConvertPng2LosslessWebPUnderPath(root_dir):
  png_list = GetFileListByMimeType(root_dir, 'image/png')
  for path in png_list:
    ConvertPng2LosslessWebP(path, path)


def ConvertPng2LossyWebPUnderPath(root_dir, quality, alpha_quality):
  png_list = GetFileListByMimeType(root_dir, 'image/png')
  for path in png_list:
    ConvertPng2LossyWebP(path, path, quality, alpha_quality)


def GetFileListByMimeType(root_path, mime_type):
  if not os.path.isdir(root_path):
    return []
  
  file_list = []
  for lists in os.listdir(root_path): 
    path = os.path.join(root_path, lists) 
    if os.path.isdir(path): 
        file_list += GetFileListByMimeType(path, mime_type)
    elif CheckFileType(path, mime_type):
      file_list.append(path)
  return file_list
