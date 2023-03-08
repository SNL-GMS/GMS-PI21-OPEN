from setuptools import setup

'''
To rebuild, run python3 setup.py
'''

VERSION = '0.1.0'

setup(
    name='gmskube',
    version=VERSION,
    description='A command line application to manage gms instances on Kubernetes',
    packages=['gmskube'],
    scripts=['gmskube/gmskube_cli.py'],
    python_requires='>=3.10',
    tests_require=['pytest==7.1.1'],
    install_requires=['pyyaml==6.0',
                      'requests==2.27.1',
                      'rich==12.5.1']
)
