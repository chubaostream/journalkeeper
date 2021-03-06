/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.journalkeeper.rpc.remoting.transport.codec;

import io.journalkeeper.rpc.remoting.transport.command.Header;
import io.journalkeeper.rpc.remoting.transport.command.Type;
import io.journalkeeper.rpc.remoting.transport.command.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * jmq消息体编解码器工厂
 * author: gaohaoxiang
 *
 * date: 2018/8/21
 */
public class PayloadCodecFactory {

    protected static final Logger logger = LoggerFactory.getLogger(PayloadCodecFactory.class);

    private final Map<Integer /** payload type **/, PayloadDecoder> decoderMapper = new HashMap<>();
    private final Map<Integer /** payload type **/, PayloadEncoder> encoderMapper = new HashMap<>();

    public PayloadDecoder getDecoder(Header header) {
        return decoderMapper.get(header.getType());
    }

    public PayloadEncoder getEncoder(Header header) {
        return encoderMapper.get(header.getType());
    }

    public void register(int type, PayloadCodec payloadCodec) {
        int[] types = new int[]{type};
        register(types, (PayloadDecoder) payloadCodec);
        register(types, (PayloadEncoder) payloadCodec);
    }

    public void register(PayloadCodec payloadCodec) {
        int[] types = getTypes(payloadCodec);
        register(types, (PayloadDecoder) payloadCodec);
        register(types, (PayloadEncoder) payloadCodec);
    }

    public void register(PayloadDecoder payloadDecoder) {
        int[] types = getTypes(payloadDecoder);
        register(types, payloadDecoder);
    }

    public void register(PayloadEncoder payloadEncoder) {
        int[] types = getTypes(payloadEncoder);
        register(types, payloadEncoder);
    }

    protected void register(int[] types, PayloadEncoder payloadEncoder) {
        if (types == null) {
            logger.error("unsupported payload encoder, encoder: {}", payloadEncoder);
            return;
        }

        for (int type : types) {
            logger.debug("register payload encoder, type: {}, encoder: {}", type, payloadEncoder);
            encoderMapper.put(type, payloadEncoder);
        }
    }

    protected void register(int[] types, PayloadDecoder payloadDecoder) {
        if (types == null) {
            logger.error("unsupported payload decoder, decoder: {}", payloadDecoder);
            return;
        }

        for (int type : types) {
            logger.debug("register payload decoder, type: {}, decoder: {}", type, payloadDecoder);
            decoderMapper.put(type, payloadDecoder);
        }
    }

    protected int[] getTypes(Object codec) {
        if (codec instanceof Types) {
            return ((Types) codec).types();
        } else if (codec instanceof Type) {
            return new int[]{((Type) codec).type()};
        } else {
            return null;
        }
    }
}