/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.vxquery.compiler.expression;

import java.util.Arrays;

import org.apache.vxquery.context.StaticContext;
import org.apache.vxquery.functions.Function;
import org.apache.vxquery.types.SequenceType;

public class ExpressionBuilder {
    private ExpressionBuilder() {
    }

    public static Expression instanceOf(StaticContext ctx, Expression expression, SequenceType type) {
        return new InstanceofExpression(ctx, expression, type);
    }

    public static Expression functionCall(StaticContext ctx, Function function, Expression... args) {
        return new FunctionCallExpression(ctx, function, Arrays.asList(args));
    }
}