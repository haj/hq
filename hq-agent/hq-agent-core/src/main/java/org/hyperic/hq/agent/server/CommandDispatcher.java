/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.agent.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.agent.AgentAPIInfo;
import org.hyperic.hq.agent.AgentCommand;
import org.hyperic.hq.agent.AgentRemoteException;
import org.hyperic.hq.agent.AgentRemoteValue;

/**
 * The object which manages all libraries wanting to have their
 * commands remotely dispatched.  Libraries can register their
 * server handler with a CommandDispatcher, and when a request
 * is processed, their registered methods will be invoked.
 */

public class CommandDispatcher {
    private Log log;     
    private Map<String, List<AgentServerHandler>> commands;

    CommandDispatcher(){
        this.commands = new HashMap<String, List<AgentServerHandler>>();
        this.log      = LogFactory.getLog(CommandDispatcher.class);
    }

    /**
     * Register a server handler with the dispatcher.  The server
     * handler will be queried as to what commands it knows about,
     * and that information will be saved locally.
     *
     * @param handler an object implementing the AgentServerHandler 
     *                interface
     *
     * @see AgentServerHandler
     */

    void addServerHandler(AgentServerHandler handler){
        String[] cmds = handler.getCommandSet();
        
        for(final String cmd: cmds){
            List<AgentServerHandler> handlerList = commands.get(cmd);
            if (handlerList == null){
                handlerList = new LinkedList<AgentServerHandler>();
            }
            handlerList.add(handler);
            this.commands.put(cmd, handlerList);
        }
    }

    public List<AgentServerHandler> getHandlers(AgentCommand command){
        return commands.get(command.getCommand());
    }
    
    /**
     * Dispatch a method after verifying that the version APIs
     * match up.
     *
     * @param cmd        Method to invoke
     * @param inStream   Stream which can read from the client
     * @param outStream  Stream which can write to the client
     *
     * @return the return value from the dispatched method
     * @throws AgentRemoteException indicating some error occurred dispatching
     *                              the method.
     */

    public AgentRemoteValue processRequest(AgentCommand cmd, AgentServerHandler handler, InputStream inStream,
                                    OutputStream outStream)
        throws AgentRemoteException
    {
        try {
            AgentAPIInfo apiInfo;

            if(handler == null){
                throw new AgentRemoteException("Unknown command, '" + 
                                               cmd.getCommand() + "'");
            }
            apiInfo = handler.getAPIInfo();
            if(!apiInfo.isCompatible(cmd.getCommandVersion())){
                throw new AgentRemoteException("Client API mismatch: " +
                                               cmd.getCommandVersion() + 
                                               " vs. " +
                                               apiInfo.getVersion());
            }
            
            return handler.dispatchCommand(cmd.getCommand(), 
                                           cmd.getCommandArg(), inStream, 
                                           outStream);
        } catch(AgentRemoteException exc){
            throw exc;
        } catch(Exception exc){
            this.log.error("Error while processing request", exc);
            throw new AgentRemoteException(exc.toString());
        } catch(LinkageError err){
            throw new AgentRemoteException(err.toString());
        }
    }
}
