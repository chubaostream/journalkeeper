/**
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
package io.journalkeeper.core.entry;

import io.journalkeeper.core.journal.ParseJournalException;
import io.journalkeeper.utils.parser.EntryParser;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author LiYue
 * Date: 2019-01-15
 */
class JournalEntryParser extends EntryParser {
    private final static short MAGIC_CODE = ByteBuffer.wrap(new byte[] {(byte) 0XF4, (byte) 0X3C}).getShort();

    private final static int VARIABLE_LENGTH = -1;
    private final static int FIXED_LENGTH_1 = 1;
    private final static int FIXED_LENGTH_2 = 2;
    private final static int FIXED_LENGTH_4 = 4;
    private static int offset = 0;
    // 第一个变长属性的偏移量
    private static int firstVarOffset = -1;
    // 第一个变长属性在attributes数组中的索引
    private static int firstVarIndex = -1;
    private final static List<Attribute> attributeList= new LinkedList<>();

    private final static int LENGTH = createAttribute("LENGTH",FIXED_LENGTH_4);
    private final static int MAGIC = createAttribute("MAGIC",FIXED_LENGTH_2);
    private final static int TERM = createAttribute("TERM",FIXED_LENGTH_4);
    private final static int PARTITION = createAttribute("PARTITION",FIXED_LENGTH_2);
    private final static int BATCH_SIZE = createAttribute("BATCH_SIZE",FIXED_LENGTH_2);
    private final static int ENTRY = createAttribute("ENTRY",VARIABLE_LENGTH);



    public static int getHeaderLength(){return firstVarOffset;}

    private static ByteBuffer getByteBuffer(ByteBuffer messageBuffer, int relativeOffset) {
        int offset = firstVarOffset;
        // 计算偏移量

        int length;
        for(int index = firstVarIndex; index < firstVarIndex - relativeOffset; index++) {
            offset += getInt(messageBuffer, LENGTH) - getHeaderLength();
        }

        length = getInt(messageBuffer, LENGTH) - getHeaderLength();
        if(length < 0) throw new ParseJournalException("Invalid offset: " + relativeOffset);

        ByteBuffer byteBuffer = messageBuffer.slice();
        byteBuffer.position(offset);
        byteBuffer.limit(offset + length);
        return byteBuffer;

    }

    public static byte [] getEntry(byte [] entryWithHeader) {
        return Arrays.copyOfRange(entryWithHeader, getHeaderLength(), entryWithHeader.length);
    }


    /**
     * 定长消息直接返回offset
     * 变长消息返回属性相对于第一个变长属性的索引值的偏移量的负值：第一个变长属性在attributes中的索引值 - 属性在attributes中的索引值
     * @param length 属性长度，定长消息为正值，负值表示变长消息。
     */
    private static int createAttribute(String name, int length){

        Attribute attribute = new Attribute(name, length);


        if(attribute.length >= 0) {
            // 定长属性
            if(offset < 0)
                throw new ParseJournalException(
                        "Can not add a fixed length attribute after any variable length attribute!");
            attribute.setOffset(offset);
            offset += length;
        } else {
            // 变长属性
            if(firstVarOffset < 0 ) { // 第一个变长属性
                firstVarOffset = offset;
                firstVarIndex = attributeList.size();
                offset = -1;
            }
            attribute.setOffset(firstVarIndex - attributeList.size());
        }


        attributeList.add(attribute);
        return attribute.getOffset();
    }

    private static class Attribute {
        private final int length;
        private final String name;
        private int offset = -1;

        /**
         * 定长属性
         * @param length 长度
         */
        Attribute(String name, int length) {
            this.name = name;
            this.length = length;
        }

        int getLength() {

            return length;
        }

        int getOffset() {
            return offset;
        }

        void setOffset(int offset) {
            this.offset = offset;
        }

        String getName() {
            return name;
        }
    }

    public static EntryHeader parseHeader(ByteBuffer headerBuffer) {
        EntryHeader header = new EntryHeader();
        checkMagic(headerBuffer);
        header.setPayloadLength(getInt(headerBuffer, LENGTH));
        header.setTerm(getInt(headerBuffer, TERM));
        header.setPartition(getShort(headerBuffer, PARTITION));
        header.setBatchSize(getShort(headerBuffer, BATCH_SIZE));

        return header;
    }

    private static void checkMagic(ByteBuffer headerBuffer) {
        if (MAGIC_CODE != getShort(headerBuffer, MAGIC)) {
            throw new ParseJournalException("Check magic failed！");
        }
    }

    public static void serialize(ByteBuffer buffer, Entry storageEntry) {

        serializeHeader(buffer, (EntryHeader )storageEntry.getHeader());
        buffer.put(storageEntry.getEntry());
    }
    public static void serializeHeader(ByteBuffer buffer, EntryHeader header) {

        buffer.putInt(header.getPayloadLength());
        buffer.putShort(MAGIC_CODE);
        buffer.putInt(header.getTerm());
        buffer.putShort((short ) header.getPartition());
        buffer.putShort((short ) header.getBatchSize());
    }
}
