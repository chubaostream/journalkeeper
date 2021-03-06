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
package io.journalkeeper.rpc.codec;

import io.journalkeeper.rpc.header.JournalKeeperHeader;
import io.journalkeeper.rpc.remoting.serialize.CodecSupport;
import io.journalkeeper.rpc.remoting.transport.command.Type;
import io.journalkeeper.rpc.server.RequestVoteRequest;
import io.netty.buffer.ByteBuf;

/**
 * @author LiYue
 * Date: 2019-04-02
 */
public class RequestVoteRequestCodec extends GenericPayloadCodec<RequestVoteRequest> implements Type {
    @Override
    protected void encodePayload(JournalKeeperHeader header, RequestVoteRequest request, ByteBuf buffer) {
//        int term, URI candidate, long lastLogIndex, int lastLogTerm
        CodecSupport.encodeInt(buffer, request.getTerm());
        CodecSupport.encodeUri(buffer, request.getCandidate());
        CodecSupport.encodeLong(buffer, request.getLastLogIndex());
        CodecSupport.encodeInt(buffer, request.getLastLogTerm());
        CodecSupport.encodeBoolean(buffer, request.isFromPreferredLeader());
        if(header.getVersion() > 1) {
            CodecSupport.encodeBoolean(buffer, request.isPreVote());
        }
    }

    @Override
    protected RequestVoteRequest decodePayload(JournalKeeperHeader header, ByteBuf buffer) {
        if(header.getVersion() > 1) {
            return new RequestVoteRequest(
                    CodecSupport.decodeInt(buffer),
                    CodecSupport.decodeUri(buffer),
                    CodecSupport.decodeLong(buffer),
                    CodecSupport.decodeInt(buffer),
                    CodecSupport.decodeBoolean(buffer),
                    CodecSupport.decodeBoolean(buffer));
        } else {
            return new RequestVoteRequest(
                    CodecSupport.decodeInt(buffer),
                    CodecSupport.decodeUri(buffer),
                    CodecSupport.decodeLong(buffer),
                    CodecSupport.decodeInt(buffer),
                    CodecSupport.decodeBoolean(buffer),
                    false);
        }
    }

    @Override
    public int type() {
        return RpcTypes.REQUEST_VOTE_REQUEST;
    }
}
