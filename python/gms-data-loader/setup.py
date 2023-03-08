from setuptools import setup, find_packages

'''
To rebuild, run python3 setup.py
'''

VERSION = '0.1.0'

setup(
  name='gms-data-loader',
  version=VERSION,
  description='A command line application for loading gms data',
  packages=find_packages(),
  include_package_data=True,
  scripts=['bin/gms-data-loader.py'],
  python_requires='>=3.10',
  install_requires=['pyyaml==6.0',
                    'requests==2.27.1',
                    'minio==7.1.7']
)
