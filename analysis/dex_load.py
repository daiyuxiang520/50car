"""公共加载器：容错解析两个 dumped dex(跳过损坏的注解)"""
import sys
sys.setrecursionlimit(10000)
from loguru import logger
logger.remove()
import androguard.core.dex as dm
_orig_parse = dm.MapItem.parse
_SKIP = {'ANNOTATION_ITEM','ANNOTATION_SET_ITEM','ANNOTATIONS_DIRECTORY_ITEM','ANNOTATION_OFF_ITEM','ANNOTATION_SET_REF_LIST'}
def _safe_parse(self):
    if self.type.name in _SKIP:
        self.item = []
        return
    return _orig_parse(self)
dm.MapItem.parse = _safe_parse
_origEV = dm.EncodedValue.__init__
_depth = [0]
def _safeEV(self, buff, cm):
    _depth[0] += 1
    try:
        if _depth[0] > 200:
            raise RuntimeError('skip annotation')
        _origEV(self, buff, cm)
    finally:
        _depth[0] -= 1
dm.EncodedValue.__init__ = _safeEV
from androguard.core.dex import DEX
def load():
    import os
    base = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'extracted', 'inner')
    return DEX(open(os.path.join(base, 'classes.dex'), 'rb').read()), DEX(open(os.path.join(base, 'classes2.dex'), 'rb').read())
