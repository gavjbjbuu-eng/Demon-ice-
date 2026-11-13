const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_pushkar";
const ADMIN_EMAIL = "gavjbjbuu@gmail.com";
const HOST_UID = "host_alpha";
const ALICE_UID = "player_alice";
const BOB_UID = "player_bob";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function ts() {
  return new Date(Date.now() - 1000);
}

function adminContext() {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  });
}

function spoofedAdminContext() {
  return testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read users, tournaments, or results", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("tournaments").where("status", "==", "UPCOMING").get());
  await assertFails(unauth.collection("results").where("mode", "==", "CS").get());
  await assertFails(unauth.collection("auditLogs").get());
});

test("User profile creation, self-privilege prevention, and PII isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@gmail.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@gmail.com", email_verified: true }).firestore();
  const now = ts();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      iceId: "ICE12345",
      name: "Alice Gamer",
      email: "alice@gmail.com",
      freeFireName: "AliceFF",
      freeFireUid: "11223344",
      creditsBalance: 0,
      isBlocked: false,
      matchesPlayed: 0,
      wins: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Ghost field rejected
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      iceId: "ICE99999",
      name: "Bob",
      email: "bob@gmail.com",
      creditsBalance: 0,
      isBlocked: false,
      matchesPlayed: 0,
      wins: 0,
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user PII read rejected
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Self-escalation of creditsBalance rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      creditsBalance: 9999,
      updatedAt: ts(),
    })
  );

  // Valid profile edit succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      freeFireName: "AliceProFF",
      updatedAt: ts(),
    })
  );
});

test("Spoofed unverified admin email cannot access admin operations", async () => {
  const spoofDb = spoofedAdminContext().firestore();
  const now = ts();
  await assertFails(
    spoofDb.collection("tournaments").doc("t1").set({
      title: "Fake CS",
      subtitle: "5v5",
      mode: "CS",
      status: "UPCOMING",
      bannerKey: "banner_cs",
      entryLabel: "Free Entry",
      prizeLabel: "500 Credits",
      totalSlots: 50,
      joinedSlots: 0,
      scheduleText: "Today 11 AM",
      rules: "Fair play",
      createdBy: "spoof_uid",
      createdByRole: "ADMIN",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Admin can create host, tournament, room details, results; host cannot publish results", async () => {
  const adminDb = adminContext().firestore();
  const hostDb = testEnv.authenticatedContext(HOST_UID, { email: "host@gmail.com", email_verified: true }).firestore();
  const now = ts();

  await assertSucceeds(
    adminDb.collection("hosts").doc(HOST_UID).set({
      hostId: HOST_UID,
      userId: HOST_UID,
      name: "Host Alpha",
      email: "host@gmail.com",
      assignedMode: "CS",
      isBlocked: false,
      createdBy: ADMIN_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    hostDb.collection("tournaments").doc("match_cs_1").set({
      title: "CS Pro Cup",
      subtitle: "5v5 - Clash Squad",
      mode: "CS",
      status: "UPCOMING",
      bannerKey: "banner_cs",
      entryLabel: "Free Entry • Non-Cash",
      prizeLabel: "500 Credits",
      totalSlots: 50,
      joinedSlots: 0,
      scheduleText: "Today • 11:00 AM",
      rules: "No hacks",
      createdBy: HOST_UID,
      createdByRole: "HOST",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    hostDb.collection("tournaments").doc("match_cs_1").collection("room").doc("details").set({
      tournamentId: "match_cs_1",
      roomId: "842671",
      roomPassword: "ICE123",
      updatedBy: HOST_UID,
      updatedAt: now,
    })
  );

  // Host CANNOT publish winning results
  await assertFails(
    hostDb.collection("results").doc("res_1").set({
      userId: ALICE_UID,
      playerName: "Alice",
      freeFireName: "AliceFF",
      tournamentId: "match_cs_1",
      tournamentTitle: "CS Pro Cup",
      mode: "CS",
      wins: 1,
      kills: 10,
      points: 100,
      rank: 1,
      rewardCredits: 250,
      publishedBy: HOST_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin CAN publish winning results
  await assertSucceeds(
    adminDb.collection("results").doc("res_1").set({
      userId: ALICE_UID,
      playerName: "Alice",
      freeFireName: "AliceFF",
      tournamentId: "match_cs_1",
      tournamentTitle: "CS Pro Cup",
      mode: "CS",
      wins: 1,
      kills: 10,
      points: 100,
      rank: 1,
      rewardCredits: 250,
      publishedBy: ADMIN_UID,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Player atomic registration unlocks room details; unjoined player cannot read room details", async () => {
  const adminDb = adminContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@gmail.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@gmail.com", email_verified: true }).firestore();
  const now = ts();

  await adminDb.collection("tournaments").doc("match_br_1").set({
    title: "BR Squad Cup",
    subtitle: "Squad",
    mode: "BR",
    status: "UPCOMING",
    bannerKey: "banner_br",
    entryLabel: "Free Entry • Non-Cash",
    prizeLabel: "1000 Credits",
    totalSlots: 100,
    joinedSlots: 0,
    scheduleText: "Today • 2:00 PM",
    rules: "Fair play",
    createdBy: ADMIN_UID,
    createdByRole: "ADMIN",
    createdAt: now,
    updatedAt: now,
  });

  await adminDb.collection("tournaments").doc("match_br_1").collection("room").doc("details").set({
    tournamentId: "match_br_1",
    roomId: "998877",
    roomPassword: "BR99",
    updatedBy: ADMIN_UID,
    updatedAt: now,
  });

  await assertFails(
    aliceDb.collection("tournaments").doc("match_br_1").collection("room").doc("details").get()
  );

  const batch = aliceDb.batch();
  batch.set(aliceDb.collection("registrations").doc(`match_br_1_${ALICE_UID}`), {
    tournamentId: "match_br_1",
    tournamentTitle: "BR Squad Cup",
    mode: "BR",
    userId: ALICE_UID,
    hostId: ADMIN_UID,
    playerName: "Alice",
    freeFireName: "AliceFF",
    freeFireUid: "112233",
    status: "CONFIRMED",
    joinedAt: now,
  });
  batch.update(aliceDb.collection("tournaments").doc("match_br_1"), {
    joinedSlots: 1,
    updatedAt: now,
  });
  await assertSucceeds(batch.commit());

  await assertSucceeds(
    aliceDb.collection("tournaments").doc("match_br_1").collection("room").doc("details").get()
  );
  await assertFails(
    bobDb.collection("tournaments").doc("match_br_1").collection("room").doc("details").get()
  );
});
