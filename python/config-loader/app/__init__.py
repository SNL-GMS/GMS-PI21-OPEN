import os
import atexit
import multiprocessing

from sqlalchemy import create_engine
from sqlalchemy.orm import scoped_session
from sqlalchemy.orm import sessionmaker

basedir = os.path.abspath(os.path.dirname(__file__))
engine = create_engine("sqlite:///{0}/state/state.db".format(basedir))
session_factory = sessionmaker(bind=engine)
Session = scoped_session(session_factory)

from .state import State

app_state = State()

from flask_executor import Executor

executor = Executor()
log_queue = multiprocessing.Queue(-1)

from .app import create_app
from .config import config_by_name
from .loader import loader
from .routes import initiate_load
from .routes import initiate_reload

application = create_app(os.getenv("FLASK_ENV") or "test")

def session_remove():
    Session.remove()
    
atexit.register(session_remove)
