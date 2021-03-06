/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.contrib.streaming.state;

import org.apache.flink.api.common.typeutils.base.IntSerializer;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.core.memory.ByteArrayInputStreamWithPos;
import org.apache.flink.core.memory.ByteArrayOutputStreamWithPos;
import org.apache.flink.core.memory.DataInputViewStreamWrapper;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.core.memory.DataOutputViewStreamWrapper;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for guarding {@link RocksDBKeySerializationUtils}.
 */
public class RocksDBKeySerializationUtilsTest {

	@Test
	public void testIsAmbiguousKeyPossible() {
		Assert.assertFalse(RocksDBKeySerializationUtils.isAmbiguousKeyPossible(
			IntSerializer.INSTANCE, StringSerializer.INSTANCE));

		Assert.assertTrue(RocksDBKeySerializationUtils.isAmbiguousKeyPossible(
			StringSerializer.INSTANCE, StringSerializer.INSTANCE));
	}

	@Test
	public void testKeyGroupSerializationAndDeserialization() throws Exception {
		ByteArrayOutputStreamWithPos outputStream = new ByteArrayOutputStreamWithPos(8);
		DataOutputView outputView = new DataOutputViewStreamWrapper(outputStream);

		for (int keyGroupPrefixBytes = 1; keyGroupPrefixBytes <= 2; ++keyGroupPrefixBytes) {
			for (int orgKeyGroup = 0; orgKeyGroup < 128; ++orgKeyGroup) {
				outputStream.reset();
				RocksDBKeySerializationUtils.writeKeyGroup(orgKeyGroup, keyGroupPrefixBytes, outputView);
				int deserializedKeyGroup = RocksDBKeySerializationUtils.readKeyGroup(
					keyGroupPrefixBytes,
					new DataInputViewStreamWrapper(new ByteArrayInputStreamWithPos(outputStream.toByteArray())));
				Assert.assertEquals(orgKeyGroup, deserializedKeyGroup);
			}
		}
	}

	@Test
	public void testKeySerializationAndDeserialization() throws Exception {
		ByteArrayOutputStreamWithPos outputStream = new ByteArrayOutputStreamWithPos(8);
		DataOutputView outputView = new DataOutputViewStreamWrapper(outputStream);

		// test for key
		for (int orgKey = 0; orgKey < 100; ++orgKey) {
			outputStream.reset();
			RocksDBKeySerializationUtils.writeKey(orgKey, IntSerializer.INSTANCE, outputStream, outputView, false);
			ByteArrayInputStreamWithPos inputStream = new ByteArrayInputStreamWithPos(outputStream.toByteArray());
			int deserializedKey = RocksDBKeySerializationUtils.readKey(IntSerializer.INSTANCE, inputStream, new DataInputViewStreamWrapper(inputStream), false);
			Assert.assertEquals(orgKey, deserializedKey);

			RocksDBKeySerializationUtils.writeKey(orgKey, IntSerializer.INSTANCE, outputStream, outputView, true);
			inputStream = new ByteArrayInputStreamWithPos(outputStream.toByteArray());
			deserializedKey = RocksDBKeySerializationUtils.readKey(IntSerializer.INSTANCE, inputStream, new DataInputViewStreamWrapper(inputStream), true);
			Assert.assertEquals(orgKey, deserializedKey);
		}
	}

	@Test
	public void testNamespaceSerializationAndDeserialization() throws Exception {
		ByteArrayOutputStreamWithPos outputStream = new ByteArrayOutputStreamWithPos(8);
		DataOutputView outputView = new DataOutputViewStreamWrapper(outputStream);

		for (int orgNamespace = 0; orgNamespace < 100; ++orgNamespace) {
			outputStream.reset();
			RocksDBKeySerializationUtils.writeNameSpace(orgNamespace, IntSerializer.INSTANCE, outputStream, outputView, false);
			ByteArrayInputStreamWithPos inputStream = new ByteArrayInputStreamWithPos(outputStream.toByteArray());
			int deserializedNamepsace = RocksDBKeySerializationUtils.readNamespace(IntSerializer.INSTANCE, inputStream, new DataInputViewStreamWrapper(inputStream), false);
			Assert.assertEquals(orgNamespace, deserializedNamepsace);

			RocksDBKeySerializationUtils.writeNameSpace(orgNamespace, IntSerializer.INSTANCE, outputStream, outputView, true);
			inputStream = new ByteArrayInputStreamWithPos(outputStream.toByteArray());
			deserializedNamepsace = RocksDBKeySerializationUtils.readNamespace(IntSerializer.INSTANCE, inputStream, new DataInputViewStreamWrapper(inputStream), true);
			Assert.assertEquals(orgNamespace, deserializedNamepsace);
		}
	}
}
