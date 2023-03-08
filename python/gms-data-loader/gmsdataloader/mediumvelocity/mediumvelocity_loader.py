from minio import Minio
import os

class MediumVelocityMinioConfig:
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
            self.minio_client = Minio(base_url, os.getenv('MINIO_ROOT_USER'), os.getenv('MINIO_ROOT_PASSWORD'), secure=False)
        else:
            self.minio_client = minio_client


class MediumVelocityLoader:
    """
        Loads medium velocity files into minio.
    """

    json_file_type = '.json'

    def __init__(self, mediumvelocity_root: str, mediumvelocity_minio_config: MediumVelocityMinioConfig):
        """
        Constructor

        Args:
            mediumvelocity_root: Path where the medium velocity files are stored
            mediumvelocity_minio_config: MediumVelocityMinioConfig objects which has config info for minio.
        """

        self.minio_client = mediumvelocity_minio_config.minio_client
        self.mediumvelocity_root = mediumvelocity_root
        self.bucket = mediumvelocity_minio_config.bucket

    def load(self):
        """
        Does the work of loading the JSON files into minio.

        Returns: nothing

        """

        if not self.minio_client.bucket_exists(self.bucket):
            self.minio_client.make_bucket(self.bucket)

        minio_prefix_map = self.get_minio_prefix_map()
        for minio_prefix in minio_prefix_map:
            self.minio_client.fput_object(
                self.bucket,
                minio_prefix,
                minio_prefix_map[minio_prefix]
            )

    def get_minio_prefix_map(self):
        """
        Creates a map, where the key is "bucketname/model-file-name"; and the value is the full file path to load.

        Returns: The map as described above.

        """

        # look at our base configuration and get anything that wasn't in overrides
        if not os.path.isdir(self.mediumvelocity_root):
            raise ValueError(f'Processing configuration root {self.mediumvelocity_root} must be a directory.')

        minio_prefix_map = {}
        for mediumvelocity_file_name in [f for f in os.listdir(self.mediumvelocity_root) if f.endswith(self.json_file_type)]:
            minio_key_prefix = os.path.split(self.mediumvelocity_root)[1] + "/" + os.path.splitext(mediumvelocity_file_name)[0]
            minio_prefix_map[minio_key_prefix] = os.path.join(self.mediumvelocity_root, mediumvelocity_file_name)

        return minio_prefix_map
