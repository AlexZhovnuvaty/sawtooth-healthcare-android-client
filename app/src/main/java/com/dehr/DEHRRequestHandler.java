package com.dehr;

import com.dehr.protobuf.Payload;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import sawtooth.sdk.protobuf.Batch;
import sawtooth.sdk.protobuf.BatchHeader;
import sawtooth.sdk.protobuf.BatchList;
import sawtooth.sdk.protobuf.Transaction;
import sawtooth.sdk.protobuf.TransactionHeader;
import sawtooth.sdk.signing.PrivateKey;
import sawtooth.sdk.signing.Secp256k1Context;
import sawtooth.sdk.signing.Signer;

class DEHRRequestHandler {

//    private var service: SawtoothRestApi? = null
    private Signer signer;

    DEHRRequestHandler(PrivateKey privateKey) {
        Secp256k1Context context = new Secp256k1Context();
        this.signer = new Signer(context, privateKey);
    }

    BatchList addPulse(int pulse, long timestamp) throws IOException {
        String publicKey = signer.getPublicKey().hex();
        Payload.AddPulse pulsePayload = Payload.AddPulse.newBuilder()
                .setPublicKey(publicKey)
                .setPulse(String.valueOf(pulse))
                .setTimestamp(String.valueOf(timestamp)).build();

        Payload.TransactionPayload transactionPayload = Payload.TransactionPayload.newBuilder()
                .setPayloadType(Payload.TransactionPayload.PayloadType.ADD_PULSE)
                .setPulse(pulsePayload).build();

        String address = Addressing.makePulseAddress(signer.getPublicKey().hex(), timestamp);

        Transaction addPulseTransaction = makeTransaction(address, address, transactionPayload);
        Batch batch = makeBatch(Collections.singletonList(addPulseTransaction));

        return BatchList.newBuilder()
                .addBatches(batch)
                .build();
    }

    private Transaction makeTransaction(String input, String output, Payload.TransactionPayload transactionPayload) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transactionPayload.writeTo(baos);
        String serializedPayload = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        TransactionHeader header = TransactionHeader.newBuilder()
                .setSignerPublicKey(signer.getPublicKey().hex())
                .setFamilyName(Addressing.TP_FAMILYNAME)
                .setFamilyVersion(Addressing.TP_VERSION)
                .addInputs(input)
                .addOutputs(output)
                .setPayloadSha512(Addressing.hash(serializedPayload))
                .setBatcherPublicKey(signer.getPublicKey().hex())
                .setNonce(UUID.randomUUID().toString())
                .build();

        String signature = signer.sign(header.toByteArray());

        return Transaction.newBuilder()
                .setHeader(header.toByteString())
                .setPayload(ByteString.copyFrom(serializedPayload, "UTF-8"))
                .setHeaderSignature(signature)
                .build();
    }

    private Batch makeBatch(List<Transaction> transactions) {
        List<String> transactionIds = new ArrayList<>();
        for(Transaction transact: transactions){
            transactionIds.add(transact.getHeaderSignature());
        }
        BatchHeader batchHeader = BatchHeader.newBuilder()
                .setSignerPublicKey(signer.getPublicKey().hex())
                .addAllTransactionIds(transactionIds)
                .build();

        String batchSignature = signer.sign(batchHeader.toByteArray());

        return Batch.newBuilder()
                .setHeader(batchHeader.toByteString())
                .addAllTransactions(transactions)
                .setHeaderSignature(batchSignature)
                .build();
    }

}
