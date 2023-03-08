from setuptools import setup, find_packages

'''
To rebuild, run python3 setup.py
'''

VERSION = '1.0.0'

setup(
    name='gms-sysconfig',
    version=VERSION,
    description='A command line application to import and export GMS system configuration data from etcd.',
    packages=find_packages(),
    scripts=['bin/gms-sysconfig'],
    install_requires=['etcd3==0.12.0',
                      'jproperties==2.1.1'],
    python_requires='>=3.10'
)
