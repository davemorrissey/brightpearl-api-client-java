/*
 * Copyright 2014 David Morrissey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.visalia.brightpearl.apiclient.client.multimessage;

import uk.co.visalia.brightpearl.apiclient.multimessage.OnFailOption;
import uk.co.visalia.brightpearl.apiclient.multimessage.ProcessingMode;

import java.util.Collections;
import java.util.List;

/**
 * For internal use only. Represents the JSON body of a multimessage request.
 */
public class MultiMessage {

    private ProcessingMode processingMode;

    private OnFailOption onFail;

    private List<MultiMessageItem> messages;

    public MultiMessage(ProcessingMode processingMode, OnFailOption onFail, List<MultiMessageItem> messages) {
        this.processingMode = processingMode;
        this.onFail = onFail;
        this.messages = Collections.unmodifiableList(messages);
    }

    public ProcessingMode getProcessingMode() {
        return processingMode;
    }

    public OnFailOption getOnFail() {
        return onFail;
    }

    public List<MultiMessageItem> getMessages() {
        return messages;
    }
}