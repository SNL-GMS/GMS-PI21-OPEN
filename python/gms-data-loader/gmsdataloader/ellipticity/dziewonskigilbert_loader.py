import io
from minio import Minio
import os

from gmsdataloader.ellipticity.dg_to_json import parse_for_model


class DziewonskiGilbertMinioConfig:
    """
        Contains configuration for minio. Includes reading credentials from the environment.
    """

    def __init__(self, base_url: str, bucket: str, minio_client: Minio = None):
        """
        Constructor

        Args:
            base_url: Base URL for minio. Must not contain 'http://' as Minio adds that itself.
            bucket: Bucket to store data
            minio_client: For testing only - the minio client to use. If not specified, default client is created.
        """

        self.base_url = base_url
        self.bucket = bucket

        if minio_client is None:
            self.minio_client = Minio(base_url, os.getenv('MINIO_ROOT_USER'), os.getenv('MINIO_ROOT_PASSWORD'),
                                      secure=False)
        else:
            self.minio_client = minio_client

class DziewonskiGilbertLoader:
    """
    Loads the Dziewonski-Gilbert ellipticity correction tables into minio
    """

    json_file_type = '.json'

    def __init__(self, dg_root: str, minio_config: DziewonskiGilbertMinioConfig):
        """
        Constructor

        Args:
            dg_root: Path where the Dziewonski-Gilbert files are stored
            minio_config: MediumVelocityMinioConfig objects which has config info for minio.
        """

        self.minio_client = minio_config.minio_client
        self.dg_root = dg_root
        self.bucket = minio_config.bucket

    def load(self):
        if not self.minio_client.bucket_exists(self.bucket):
            self.minio_client.make_bucket(self.bucket) 

        phase_map = parse_for_model("ak135", self.dg_root)
        for phase in phase_map:
            json_encoded = phase_map[phase].encode()
            json_bytes = io.BytesIO(json_encoded)
            self.minio_client.put_object(
                self.bucket,
                "dziewonski-gilbert/" + phase,
                json_bytes,
                json_bytes.getbuffer().nbytes
            )
