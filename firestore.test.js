const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated users cannot read or write user profiles, favorites, or quiz attempts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      totalPoints: 50,
      dailyGoalPoints: 100,
      todayPoints: 50,
      currentStreak: 1,
      bestStreak: 1,
      totalLearningSeconds: 120,
      quizTotalAnswered: 5,
      quizTotalCorrect: 4,
      lastActiveDate: "2026-11-19",
      unlockedAchievementIds: ["beginner"],
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("quiz_attempts").get()
  );
});

test("Owner can create, partially update, and read their UserCloudProfile while extra/tampered fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Aklilu",
      totalPoints: 120,
      dailyGoalPoints: 100,
      todayPoints: 40,
      currentStreak: 3,
      bestStreak: 5,
      totalLearningSeconds: 600,
      quizTotalAnswered: 10,
      quizTotalCorrect: 9,
      lastActiveDate: "2026-11-19",
      unlockedAchievementIds: ["beginner", "alphabet"],
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalPoints: 145,
      todayPoints: 65,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject createdAt tamper
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalPoints: 150,
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Cross-user isolation: Alice cannot read or write Bob's profile, favorites, or quiz attempts", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      totalPoints: 10,
      dailyGoalPoints: 50,
      todayPoints: 10,
      currentStreak: 1,
      bestStreak: 1,
      totalLearningSeconds: 60,
      quizTotalAnswered: 1,
      quizTotalCorrect: 1,
      lastActiveDate: "2026-11-19",
      unlockedAchievementIds: [],
      createdAt: created,
      updatedAt: created,
    });
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("favorites")
      .doc("PROVERB_p1")
      .set({
        userId: BOB_UID,
        itemType: "PROVERB",
        itemId: "p1",
        createdAt: created,
        updatedAt: created,
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      totalPoints: 999,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("favorites").get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("favorites")
      .doc("PROVERB_p1")
      .delete()
  );
});

test("Owner can manage CloudFavorite and create immutable CloudQuizAttempt records", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("PROVERB_p1")
      .set({
        userId: ALICE_UID,
        itemType: "PROVERB",
        itemId: "p1",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );

  // Invalid favorite itemType rejected
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("favorites")
      .doc("INVALID_1")
      .set({
        userId: ALICE_UID,
        itemType: "UNKNOWN",
        itemId: "x1",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("quiz_attempts")
      .doc("attempt_1")
      .set({
        userId: ALICE_UID,
        mode: "DAILY_MIX",
        score: 4,
        totalQuestions: 5,
        pointsEarned: 65,
        createdAt: now,
      })
  );

  // Quiz attempts are immutable (updates rejected)
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("quiz_attempts")
      .doc("attempt_1")
      .update({ score: 5 })
  );
});
