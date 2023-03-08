from minio import Minio
import os


class EarthModelMinioConfig:
    """
        Contains configuration for minio. Includes reading credentials from the environment.
    """

    def __init__(self, base_url: str, bucket: str, minio_client: Minio = None) -> None:
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


class EarthModelsLoader:
    """
        Loads earth model files from into minio.
    """

    json_file_type = '.json'

    def __init__(self, earthmodels_root: str, earth_model_minio_config: EarthModelMinioConfig) -> None:
        """
        Constructor

        Args:
            earthmodels_root: Path where the JSON models are stored; this is the root, which contains directories for
            each model, and each of those directories contains model files.
            earth_model_minio_config: EarthModelMinioConfig objects which has config info for minio.
        """

        self.minio_client = earth_model_minio_config.minio_client
        self.earthmodels_root = earthmodels_root
        self.bucket = earth_model_minio_config.bucket

    def load(self) -> None:
        """
        Does the work of loading the JSON files into minio.

        Returns: nothing

        """

        if not self.minio_client.bucket_exists(self.bucket):
            self.minio_client.make_bucket(self.bucket)

        minio_prefix_map = self.get_minio_prefix_map()
        for minio_prefix in minio_prefix_map:
            for object_descriptor in minio_prefix_map[minio_prefix]:
                self.minio_client.fput_object(
                    self.bucket,
                    minio_prefix + "/" + object_descriptor['phase'],
                    object_descriptor['path']
                )

    def get_minio_prefix_map(self) -> dict:
        """
        Creates a map, where the key is "bucketname/model/model-file-name-minus-tilde", where "model-file-name-minus-tilde
        is the name of the file with any leading tilde removed; and the value is the full file path to load.

        Returns: The map as described above.

        """

        # look at our base configuration and get anything that wasn't in overrides
        if not os.path.isdir(self.earthmodels_root):
            raise ValueError(f'Processing configuration root {self.earthmodels_root} must be a directory.')

        minio_prefix_map = {}
        for name, path in [(f.name, f.path) for f in os.scandir(self.earthmodels_root) if f.is_dir()]:

            for modelname, modelpath in [(f.name, f.path) for f in os.scandir(path) if f.is_dir()]:
                minio_key_prefix = name + "/" + modelname
                if minio_key_prefix not in minio_prefix_map:
                    minio_prefix_map[minio_key_prefix] = []
                for phase_file_name in [f for f in os.listdir(modelpath) if f.endswith(self.json_file_type)]:
                    minio_prefix_map[minio_key_prefix].append(
                        {'phase': phase_file_name.replace("~", "").replace(self.json_file_type, ""),
                         'path': os.path.join(modelpath, phase_file_name)}
                    )

        return minio_prefix_map
