package com.example.geobot;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class LuaRunner {

    public interface Logger {
        void log(String msg);
    }

    public static void runScript(String script, SerialManager serialManager, Logger logger) {
        try {
            Globals globals = JsePlatform.standardGlobals();

            // Bind serial function
            globals.set("serial", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    String cmd = arg.tojstring();
                    logger.log("Serial: " + cmd);
                    serialManager.send(cmd);
                    return LuaValue.NIL;
                }
            });

            // Bind wait(ms) function
            globals.set("wait", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    int ms = arg.toint();
                    try {
                        Thread.sleep(ms);
                    } catch (InterruptedException ignored) {}
                    return LuaValue.NIL;
                }
            });

            logger.log("Executing Lua...");
            LuaValue chunk = globals.load(script);
            chunk.call();
            logger.log("Lua script finished.");

        } catch (Exception e) {
            logger.log("Lua error: " + e.getMessage());
        }
    }
              }
