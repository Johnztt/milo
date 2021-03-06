/*
 * Copyright (c) 2016 Kevin Herron
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * 	http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * 	http://www.eclipse.org/org/documents/edl-v10.html.
 */

package org.eclipse.milo.opcua.sdk.client.api.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult;
import org.eclipse.milo.opcua.stack.core.types.structured.CallResponse;

import static com.google.common.collect.Lists.newArrayList;

public interface MethodServices {

    /**
     * This service is used to call (invoke) a list of methods. Each method call is invoked within the context of an
     * existing session. If the session is terminated, the results of the method’s execution cannot be returned to the
     * client and are discarded. This is independent of the task actually performed at the server.
     *
     * @param methodsToCall a list of methods to call.
     * @return a {@link CompletableFuture} containing the {@link CallResponse}.
     */
    CompletableFuture<CallResponse> call(List<CallMethodRequest> methodsToCall);

    /**
     * Call (invoke) a method.
     *
     * @param request the {@link CallMethodRequest} describing the method to invoke.
     * @return a {@link CompletableFuture} containing the {@link CallMethodResult}.
     * @see #call(List)
     */
    default CompletableFuture<CallMethodResult> call(CallMethodRequest request) {
        return call(newArrayList(request))
            .thenApply(response -> response.getResults()[0]);
    }

}
