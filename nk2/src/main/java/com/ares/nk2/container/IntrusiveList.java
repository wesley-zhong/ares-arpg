package com.ares.nk2.container;

import com.ares.nk2.tool.FunctionUtil;
import com.ares.nk2.tool.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.security.InvalidParameterException;

@NotThreadSafe
public class IntrusiveList<Elem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntrusiveList.class);

    private final String listName;
    private IntrusiveElem _head = new IntrusiveElem();
    private IntrusiveElem _tail = new IntrusiveElem();
    private int _count = 0;

    static public class IntrusiveElem<Elem> {
        private IntrusiveList<Elem> _flag = null;
        private Elem _ptr = null;
        private IntrusiveElem _prev = null;
        private IntrusiveElem _next = null;

        IntrusiveElem() {
        }

        void clearPtr() {
            _prev = null;
            _next = null;
            _flag = null;
        }

        public void attatch(Elem o) {
            _ptr = o;
        }

        public Elem getData() {
            return _ptr;
        }

        public IntrusiveElem<Elem> next() {
            return _next;
        }

        public IntrusiveElem<Elem> prev() {
            return _prev;
        }

        public boolean isInList() {
            return _flag != null;
        }

        public void remove() {
            if (isInList()) {
                _flag.delete(this);
            }
        }
    }

    public boolean contains(IntrusiveElem e) {
        return e._flag == this;
    }

    static public <Elem> IntrusiveElem getElem(Elem e) {
        IntrusiveElem elem = new IntrusiveElem();
        elem.attatch(e);
        return elem;
    }

    public IntrusiveList(String listName) {
        this.listName = listName;
        _head._next = _tail;
        _tail._prev = _head;
    }

    public void insert(IntrusiveElem elem) {
        if (elem._flag != null) {
            LOGGER.error(StringFormatter.format("elem {} try to insert to {}, but already in {}", elem, listName, elem._flag.listName));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(FunctionUtil.getStackTrace());
            }
            throw new InvalidParameterException("IntrusiveElem " + elem);
        }
        elem._next = _tail;
        elem._prev = _tail._prev;
        _tail._prev._next = elem;
        _tail._prev = elem;
        elem._flag = this;
        ++_count;
    }

    public void delete(IntrusiveElem elem) {
        if (elem._flag != this) {
            if (elem._flag != null) {
                throw new InvalidParameterException("IntrusiveElem " + elem);
            }
            return;
        }
        elem._prev._next = elem._next;
        elem._next._prev = elem._prev;
        elem.clearPtr();
        --_count;
    }

    public int size() {
        return _count;
    }

    public IntrusiveElem<Elem> head() {
        return _head;
    }

    public IntrusiveElem<Elem> end() {
        return _tail;
    }

    public IntrusiveElem<Elem> first() {
        return _head._next;
    }

    public boolean isEmpty() {
        return _count <= 0;
    }

    public void clear() {
        IntrusiveElem elem = first();
        while (elem != end()) {
            delete(elem);
            elem = first();
        }
    }
}
