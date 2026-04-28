package com.example.queuemanagementsystem.data.repository;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.example.queuemanagementsystem.data.model.QueueStatus;
import com.example.queuemanagementsystem.data.model.ServiceItem;
import com.example.queuemanagementsystem.data.model.SlotItem;
import com.example.queuemanagementsystem.data.model.TokenItem;
import com.example.queuemanagementsystem.firebase.FirebaseManager;
import com.example.queuemanagementsystem.utils.Constants;
import com.example.queuemanagementsystem.utils.SimpleCallback;
import com.example.queuemanagementsystem.utils.SimpleListCallback;
import com.example.queuemanagementsystem.utils.TokenUtils;
import com.example.queuemanagementsystem.utils.VoidCallback;
import com.example.queuemanagementsystem.data.model.AdminAnalytics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueueRepository {

    public void getServices(SimpleListCallback<ServiceItem> callback) {
        FirebaseManager.db()
                .collection(Constants.SERVICES)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ServiceItem> services = new ArrayList<>();
                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        ServiceItem item = snapshot.toObject(ServiceItem.class);
                        if (item != null && item.isActive()) {
                            item.setServiceId(snapshot.getId());
                            services.add(item);
                        }
                    }
                    services.sort(Comparator.comparing(ServiceItem::getName));
                    callback.onSuccess(services);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getSlots(String serviceId, SimpleListCallback<SlotItem> callback) {
        FirebaseManager.db()
                .collection(Constants.SLOTS)
                .whereEqualTo("serviceId", serviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SlotItem> slots = new ArrayList<>();
                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        SlotItem slot = snapshot.toObject(SlotItem.class);
                        if (slot != null && slot.isActive()) {
                            slot.setSlotId(snapshot.getId());
                            slots.add(slot);
                        }
                    }
                    slots.sort(Comparator.comparing(SlotItem::getDate).thenComparing(SlotItem::getStartTime));
                    callback.onSuccess(slots);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getServiceById(String serviceId, SimpleCallback<ServiceItem> callback) {
        FirebaseManager.db()
                .collection(Constants.SERVICES)
                .document(serviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ServiceItem item = snapshot.toObject(ServiceItem.class);
                    if (item == null) {
                        callback.onError(new IllegalStateException("Service not found."));
                        return;
                    }
                    item.setServiceId(snapshot.getId());
                    callback.onSuccess(item);
                })
                .addOnFailureListener(callback::onError);
    }

    public void bookToken(String serviceId, String slotId, SimpleCallback<TokenItem> callback) {
        String userId = new AuthRepository().currentUserId();
        if (userId == null) {
            callback.onError(new IllegalStateException("User not logged in."));
            return;
        }

        DocumentReference serviceRef = FirebaseManager.db().collection(Constants.SERVICES).document(serviceId);
        DocumentReference slotRef = FirebaseManager.db().collection(Constants.SLOTS).document(slotId);
        DocumentReference queueRef = FirebaseManager.db().collection(Constants.QUEUE_STATUS).document(serviceId);
        DocumentReference tokenRef = FirebaseManager.db().collection(Constants.TOKENS).document();

        FirebaseManager.db().runTransaction(transaction -> {
                    DocumentSnapshot serviceSnap = transaction.get(serviceRef);
                    DocumentSnapshot slotSnap = transaction.get(slotRef);
                    DocumentSnapshot queueSnap = transaction.get(queueRef);
                    return createBookedToken(
                            transaction,
                            serviceRef,
                            serviceSnap,
                            slotRef,
                            slotSnap,
                            queueRef,
                            queueSnap,
                            tokenRef,
                            userId,
                            null
                    );
                }).addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    public void getLatestTokenForCurrentUser(SimpleCallback<TokenItem> callback) {
        String userId = new AuthRepository().currentUserId();
        if (userId == null) {
            callback.onError(new IllegalStateException("User not logged in."));
            return;
        }

        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TokenItem> items = toTokenList(querySnapshot);
                    if (items.isEmpty()) {
                        callback.onError(new IllegalStateException("No token found for this user."));
                        return;
                    }
                    items.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    for (TokenItem item : items) {
                        if (Constants.STATUS_WAITING.equals(item.getStatus())
                                || Constants.STATUS_CALLED.equals(item.getStatus())) {
                            callback.onSuccess(item);
                            return;
                        }
                    }
                    items.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(items.get(0));
                })
                .addOnFailureListener(callback::onError);
    }

    public void getUserTokenHistory(SimpleListCallback<TokenItem> callback) {
        String userId = new AuthRepository().currentUserId();
        if (userId == null) {
            callback.onError(new IllegalStateException("User not logged in."));
            return;
        }

        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TokenItem> items = toTokenList(querySnapshot);
                    items.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    public ListenerRegistration observeToken(String tokenId, SimpleCallback<TokenItem> callback) {
        return FirebaseManager.db()
                .collection(Constants.TOKENS)
                .document(tokenId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        callback.onError(new IllegalStateException("Token not found."));
                        return;
                    }
                    TokenItem item = snapshot.toObject(TokenItem.class);
                    if (item == null) {
                        callback.onError(new IllegalStateException("Token data missing."));
                        return;
                    }
                    item.setTokenId(snapshot.getId());
                    callback.onSuccess(item);
                });
    }

    public void getTokenById(String tokenId, SimpleCallback<TokenItem> callback) {
        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .document(tokenId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    TokenItem item = snapshot.toObject(TokenItem.class);
                    if (item == null) {
                        callback.onError(new IllegalStateException("Token not found."));
                        return;
                    }
                    item.setTokenId(snapshot.getId());
                    callback.onSuccess(item);
                })
                .addOnFailureListener(callback::onError);
    }


    public ListenerRegistration observeQueueStatus(String serviceId, SimpleCallback<QueueStatus> callback) {
        return FirebaseManager.db()
                .collection(Constants.QUEUE_STATUS)
                .document(serviceId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        QueueStatus status = new QueueStatus();
                        status.setServiceId(serviceId);
                        status.setCurrentQueueNumber(0);
                        status.setCurrentTokenNumber("Not started");
                        status.setPaused(false);
                        status.setLastUpdated(System.currentTimeMillis());
                        callback.onSuccess(status);
                        return;
                    }
                    QueueStatus status = snapshot.toObject(QueueStatus.class);
                    if (status == null) {
                        callback.onError(new IllegalStateException("Queue status is empty."));
                        return;
                    }
                    status.setServiceId(snapshot.getId());
                    callback.onSuccess(status);
                });
    }

    public void getAnalytics(String serviceId, int lastDays, SimpleCallback<AdminAnalytics> callback) {
        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .whereEqualTo("serviceId", serviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> callback.onSuccess(
                        buildAnalytics(filterByDays(toTokenList(querySnapshot), lastDays))
                ))
                .addOnFailureListener(callback::onError);
    }

    public void getReportTokens(String serviceId, int lastDays, SimpleListCallback<TokenItem> callback) {
        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .whereEqualTo("serviceId", serviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TokenItem> filtered = filterByDays(toTokenList(querySnapshot), lastDays);
                    filtered.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(filtered);
                })
                .addOnFailureListener(callback::onError);
    }

    public void cancelToken(String tokenId, String reason, VoidCallback callback) {
        DocumentReference tokenRef = FirebaseManager.db().collection(Constants.TOKENS).document(tokenId);
        FirebaseManager.db().runTransaction(transaction -> {
                    DocumentSnapshot tokenSnap = transaction.get(tokenRef);
                    if (!tokenSnap.exists()) {
                        throw new IllegalStateException("Token not found.");
                    }

                    String status = tokenSnap.getString("status");
                    if (Constants.STATUS_CALLED.equals(status) || Constants.STATUS_COMPLETED.equals(status)) {
                        throw new IllegalStateException("This token can no longer be cancelled.");
                    }

                    String slotId = tokenSnap.getString("slotId");
                    DocumentReference slotRef = FirebaseManager.db().collection(Constants.SLOTS).document(slotId);
                    DocumentSnapshot slotSnap = transaction.get(slotRef);
                    Long bookedCount = slotSnap.getLong("bookedCount");
                    long safeBookedCount = bookedCount != null ? bookedCount : 0;

                    transaction.update(tokenRef,
                            "status", Constants.STATUS_CANCELLED,
                            "cancellationReason", reason,
                            "lastUpdated", System.currentTimeMillis());
                    if (safeBookedCount > 0) {
                        transaction.update(slotRef, "bookedCount", safeBookedCount - 1);
                    }
                    return null;
                }).addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void rescheduleToken(TokenItem originalToken, String newSlotId, SimpleCallback<TokenItem> callback) {
        if (originalToken == null) {
            callback.onError(new IllegalArgumentException("Token missing."));
            return;
        }

        String userId = new AuthRepository().currentUserId();
        if (userId == null) {
            callback.onError(new IllegalStateException("User not logged in."));
            return;
        }

        DocumentReference originalTokenRef = FirebaseManager.db()
                .collection(Constants.TOKENS)
                .document(originalToken.getTokenId());
        DocumentReference oldSlotRef = FirebaseManager.db()
                .collection(Constants.SLOTS)
                .document(originalToken.getSlotId());
        DocumentReference newSlotRef = FirebaseManager.db()
                .collection(Constants.SLOTS)
                .document(newSlotId);
        DocumentReference serviceRef = FirebaseManager.db()
                .collection(Constants.SERVICES)
                .document(originalToken.getServiceId());
        DocumentReference queueRef = FirebaseManager.db()
                .collection(Constants.QUEUE_STATUS)
                .document(originalToken.getServiceId());
        DocumentReference newTokenRef = FirebaseManager.db()
                .collection(Constants.TOKENS)
                .document();

        FirebaseManager.db().runTransaction(transaction -> {
                    DocumentSnapshot originalTokenSnap = transaction.get(originalTokenRef);
                    DocumentSnapshot oldSlotSnap = transaction.get(oldSlotRef);
                    DocumentSnapshot newSlotSnap = transaction.get(newSlotRef);
                    DocumentSnapshot serviceSnap = transaction.get(serviceRef);
                    DocumentSnapshot queueSnap = transaction.get(queueRef);

                    String oldStatus = originalTokenSnap.getString("status");
                    if (Constants.STATUS_CALLED.equals(oldStatus) || Constants.STATUS_COMPLETED.equals(oldStatus)) {
                        throw new IllegalStateException("This token can no longer be rescheduled.");
                    }

                    Long oldBookedCount = oldSlotSnap.getLong("bookedCount");
                    long safeOldBookedCount = oldBookedCount != null ? oldBookedCount : 0;
                    if (safeOldBookedCount > 0) {
                        transaction.update(oldSlotRef, "bookedCount", safeOldBookedCount - 1);
                    }
                    transaction.update(originalTokenRef,
                            "status", Constants.STATUS_RESCHEDULED,
                            "lastUpdated", System.currentTimeMillis());

                    return createBookedToken(
                            transaction,
                            serviceRef,
                            serviceSnap,
                            newSlotRef,
                            newSlotSnap,
                            queueRef,
                            queueSnap,
                            newTokenRef,
                            userId,
                            originalToken.getTokenId()
                    );
                }).addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    public void getPendingTokensForService(String serviceId, SimpleListCallback<TokenItem> callback) {
        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .whereEqualTo("serviceId", serviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TokenItem> items = new ArrayList<>();
                    for (TokenItem item : toTokenList(querySnapshot)) {
                        if (Constants.STATUS_WAITING.equals(item.getStatus())
                                || Constants.STATUS_CALLED.equals(item.getStatus())) {
                            items.add(item);
                        }
                    }
                    items.sort(Comparator.comparingLong(TokenItem::getQueueNumber));
                    callback.onSuccess(items);
                })
                .addOnFailureListener(callback::onError);
    }

    public void callNextToken(String serviceId, VoidCallback callback) {
        advanceQueue(serviceId, false, callback);
    }

    public void skipCurrentToken(String serviceId, VoidCallback callback) {
        advanceQueue(serviceId, true, callback);
    }

    public void togglePauseQueue(String serviceId, boolean paused, VoidCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("paused", paused);
        data.put("lastUpdated", System.currentTimeMillis());
        FirebaseManager.db()
                .collection(Constants.QUEUE_STATUS)
                .document(serviceId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void markCompletedByQr(String qrValue, VoidCallback callback) {
        String[] parts = qrValue.split("\\|");
        if (parts.length < 1) {
            callback.onError(new IllegalArgumentException("Invalid QR data."));
            return;
        }
        markTokenStatus(parts[0], Constants.STATUS_COMPLETED, callback);
    }

    public void createSlot(
            String serviceId,
            String date,
            String startTime,
            String endTime,
            long capacity,
            VoidCallback callback
    ) {
        DocumentReference slotRef = FirebaseManager.db().collection(Constants.SLOTS).document();
        Map<String, Object> data = new HashMap<>();
        data.put("slotId", slotRef.getId());
        data.put("serviceId", serviceId);
        data.put("date", date);
        data.put("startTime", startTime);
        data.put("endTime", endTime);
        data.put("capacity", capacity);
        data.put("bookedCount", 0);
        data.put("active", true);

        slotRef.set(data)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    private void advanceQueue(String serviceId, boolean skipCurrent, VoidCallback callback) {
        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .whereEqualTo("serviceId", serviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TokenItem> items = toTokenList(querySnapshot);
                    items.sort(Comparator.comparingLong(TokenItem::getQueueNumber));

                    TokenItem currentCalled = null;
                    TokenItem nextWaiting = null;
                    for (TokenItem item : items) {
                        if (Constants.STATUS_CALLED.equals(item.getStatus())) {
                            currentCalled = item;
                        } else if (Constants.STATUS_WAITING.equals(item.getStatus()) && nextWaiting == null) {
                            nextWaiting = item;
                        }
                    }

                    WriteBatch batch = FirebaseManager.db().batch();
                    DocumentReference queueRef = FirebaseManager.db()
                            .collection(Constants.QUEUE_STATUS)
                            .document(serviceId);

                    if (currentCalled != null) {
                        batch.update(
                                FirebaseManager.db().collection(Constants.TOKENS).document(currentCalled.getTokenId()),
                                "status",
                                skipCurrent ? Constants.STATUS_SKIPPED : Constants.STATUS_COMPLETED,
                                "lastUpdated",
                                System.currentTimeMillis()
                        );
                    }

                    Map<String, Object> queueMap = new HashMap<>();
                    queueMap.put("lastUpdated", System.currentTimeMillis());

                    if (nextWaiting != null) {
                        batch.update(
                                FirebaseManager.db().collection(Constants.TOKENS).document(nextWaiting.getTokenId()),
                                "status",
                                Constants.STATUS_CALLED,
                                "lastUpdated",
                                System.currentTimeMillis()
                        );
                        queueMap.put("currentQueueNumber", nextWaiting.getQueueNumber());
                        queueMap.put("currentTokenNumber", nextWaiting.getTokenNumber());
                    } else if (currentCalled != null) {
                        queueMap.put("currentQueueNumber", currentCalled.getQueueNumber());
                        queueMap.put("currentTokenNumber", currentCalled.getTokenNumber());
                    } else {
                        queueMap.put("currentQueueNumber", 0);
                        queueMap.put("currentTokenNumber", "No tokens");
                    }

                    batch.set(queueRef, queueMap, SetOptions.merge());
                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    private void markTokenStatus(String tokenId, String status, VoidCallback callback) {
        FirebaseManager.db()
                .collection(Constants.TOKENS)
                .document(tokenId)
                .update(
                        "status", status,
                        "lastUpdated", System.currentTimeMillis()
                )
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    private TokenItem createBookedToken(
            com.google.firebase.firestore.Transaction transaction,
            DocumentReference serviceRef,
            DocumentSnapshot serviceSnap,
            DocumentReference slotRef,
            DocumentSnapshot slotSnap,
            DocumentReference queueRef,
            DocumentSnapshot queueSnap,
            DocumentReference tokenRef,
            String userId,
            String rescheduledFromTokenId
    ) {
        String prefix = serviceSnap.getString("prefix");
        String serviceName = serviceSnap.getString("name");
        Long capacity = slotSnap.getLong("capacity");
        Long bookedCount = slotSnap.getLong("bookedCount");
        Long nextQueueNumber = slotSnap.getLong("nextQueueNumber");
        String date = slotSnap.getString("date");
        String startTime = slotSnap.getString("startTime");
        String endTime = slotSnap.getString("endTime");

        long safeCapacity = capacity != null ? capacity : 0;
        long safeBookedCount = bookedCount != null ? bookedCount : 0;
        long safeNextQueueNumber = nextQueueNumber != null ? nextQueueNumber : safeBookedCount;

        if (safeBookedCount >= safeCapacity) {
            throw new IllegalStateException("This slot is already full.");
        }

        long queueNumber = safeNextQueueNumber + 1;
        String tokenNumber = TokenUtils.buildTokenNumber(prefix != null ? prefix : "A", queueNumber);
        String qrValue = tokenRef.getId() + "|" + userId + "|" + serviceRef.getId() + "|" + slotRef.getId();
        long now = System.currentTimeMillis();

        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("tokenId", tokenRef.getId());
        tokenMap.put("userId", userId);
        tokenMap.put("serviceId", serviceRef.getId());
        tokenMap.put("slotId", slotRef.getId());
        tokenMap.put("tokenNumber", tokenNumber);
        tokenMap.put("queueNumber", queueNumber);
        tokenMap.put("status", Constants.STATUS_WAITING);
        tokenMap.put("createdAt", now);
        tokenMap.put("lastUpdated", now);
        tokenMap.put("qrValue", qrValue);
        tokenMap.put("userName", serviceName);
        tokenMap.put("serviceName", serviceName);
        tokenMap.put("slotDate", date);
        tokenMap.put("slotStartTime", startTime);
        tokenMap.put("slotEndTime", endTime);
        if (rescheduledFromTokenId != null) {
            tokenMap.put("rescheduledFromTokenId", rescheduledFromTokenId);
        }

        transaction.update(
                slotRef,
                "bookedCount", safeBookedCount + 1,
                "nextQueueNumber", queueNumber
        );
        transaction.set(tokenRef, tokenMap);

        if (!queueSnap.exists()) {
            Map<String, Object> queueMap = new HashMap<>();
            queueMap.put("serviceId", serviceRef.getId());
            queueMap.put("currentQueueNumber", 0);
            queueMap.put("currentTokenNumber", "Not started");
            queueMap.put("paused", false);
            queueMap.put("lastUpdated", now);
            transaction.set(queueRef, queueMap, SetOptions.merge());
        }

        TokenItem tokenItem = new TokenItem();
        tokenItem.setTokenId(tokenRef.getId());
        tokenItem.setUserId(userId);
        tokenItem.setServiceId(serviceRef.getId());
        tokenItem.setSlotId(slotRef.getId());
        tokenItem.setTokenNumber(tokenNumber);
        tokenItem.setQueueNumber(queueNumber);
        tokenItem.setStatus(Constants.STATUS_WAITING);
        tokenItem.setCreatedAt(now);
        tokenItem.setLastUpdated(now);
        tokenItem.setQrValue(qrValue);
        tokenItem.setServiceName(serviceName);
        tokenItem.setSlotDate(date);
        tokenItem.setSlotStartTime(startTime);
        tokenItem.setSlotEndTime(endTime);
        return tokenItem;
    }
    @NonNull
    private List<TokenItem> toTokenList(QuerySnapshot querySnapshot) {
        List<TokenItem> items = new ArrayList<>();
        for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
            TokenItem item = snapshot.toObject(TokenItem.class);
            if (item != null) {
                item.setTokenId(snapshot.getId());
                items.add(item);
            }
        }
        return items;
    }

    private List<TokenItem> filterByDays(List<TokenItem> items, int lastDays) {
        if (lastDays <= 0) {
            return items;
        }
        long minTimestamp = System.currentTimeMillis() - (lastDays * 24L * 60L * 60L * 1000L);
        List<TokenItem> filtered = new ArrayList<>();
        for (TokenItem item : items) {
            if (item.getCreatedAt() >= minTimestamp) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private AdminAnalytics buildAnalytics(List<TokenItem> items) {
        AdminAnalytics analytics = new AdminAnalytics();
        analytics.setTotalTokens(items.size());

        long waitMinutes = 0;
        long completedCount = 0;
        for (TokenItem item : items) {
            String status = item.getStatus();
            if (status == null) {
                continue;
            }
            switch (status) {
                case Constants.STATUS_WAITING:
                    analytics.setWaitingTokens(analytics.getWaitingTokens() + 1);
                    break;
                case Constants.STATUS_CALLED:
                    analytics.setCalledTokens(analytics.getCalledTokens() + 1);
                    break;
                case Constants.STATUS_COMPLETED:
                    analytics.setCompletedTokens(analytics.getCompletedTokens() + 1);
                    completedCount++;
                    waitMinutes += Math.max(1, (item.getLastUpdated() - item.getCreatedAt()) / (60 * 1000));
                    break;
                case Constants.STATUS_CANCELLED:
                    analytics.setCancelledTokens(analytics.getCancelledTokens() + 1);
                    break;
                case Constants.STATUS_SKIPPED:
                    analytics.setSkippedTokens(analytics.getSkippedTokens() + 1);
                    break;
                default:
                    break;
            }
        }

        analytics.setAverageWaitMinutes(completedCount == 0 ? 0 : waitMinutes / completedCount);
        return analytics;
    }
}