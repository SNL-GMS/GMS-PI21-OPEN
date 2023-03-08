from setuptools import setup, find_packages

'''
To rebuild, run python3 setup.py
'''

VERSION = '0.1.0'

setup(
    name='config-loader',
    version=VERSION,
    description='Application for the loading of the config into the GMS',
    packages=find_packages(),
    python_requires='>=3.10',
    install_requires=['flask==2.1.1',
                      'gunicorn==20.1.0',
                      'pyyaml==6.0',
                      'flask-sqlalchemy==2.5.1',
                      'flask-executor==0.10.0',
                      'requests==2.27.1']
)
