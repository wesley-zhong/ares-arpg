package com.ares.nk2.coroutine;

import com.ares.nk2.tool.FunctionUtil;
import com.ares.nk2.tool.NKStringFormater;
import com.ares.nk2.tool.ThreadStateMonitor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class NoAsyncCheck implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoAsyncCheck.class);

    private CoroHandle coroHandle = null;

    private NoAsyncCheck() {
    }

    public static NoAsyncCheck newInstance() {
        return new NoAsyncCheck();
    }

    public NoAsyncCheck begin() {
        CoroHandle currentCoroHandle = CoroHandle.current();
        if (currentCoroHandle == null) {
            return this;
        }

        coroHandle = currentCoroHandle;
        coroHandle.setNoAsyncCheck(this);
        return this;
    }

    public boolean end() {
        if (coroHandle == null) {
            return true;
        }
        coroHandle.setNoAsyncCheck(null);

        CoroHandle currentCoroHandle = CoroHandle.current();
        if (currentCoroHandle == null) {
            LOGGER.error(NKStringFormater.format("NoAsyncCheck.end called not in coroutine env, caller: {}", FunctionUtil.getCallerInfo(0)));
            return false;
        }

        if (currentCoroHandle != coroHandle) {
            LOGGER.error(NKStringFormater.format("NoAsyncCheck.begin CoroHandle != NoAsyncCheck.end CoroHandle, caller: {}", FunctionUtil.getCallerInfo(0)));
            return false;
        }
        return true;
    }

    public void onPark() {
        LOGGER.error(NKStringFormater.format("NoAsyncCheck park, stack trace: {}", ThreadStateMonitor.getStackTraceSelfClass()));
        coroHandle.setNoAsyncCheck(null);
        System.exit(-1);
    }

    @Override
    public void close() {
        end();
    }
}
