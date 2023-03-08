from setuptools import setup, find_packages
"""
To rebuild, run python3 setup.py
"""

VERSION = "0.1.0"

setup(
    name="gms_system_test",
    version=VERSION,
    description=(
        "A module for standing up an instance of GMS, running test "
        "augmentations against it, and tearing it down."
    ),
    packages=find_packages(),
    python_requires=">=3.10",
    install_requires=["minio==7.1.7",
                      "rich==12.5.1",
                      "tenacity==8.0.1"]
)
