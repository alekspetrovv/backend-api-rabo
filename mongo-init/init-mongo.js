const appDB = process.env.APP_MONGO_DB;
const appUser = process.env.APP_MONGO_USER;
const appPassword = process.env.APP_MONGO_PASSWORD;

if (!appDB || !appUser || !appPassword) {
    print("Error: Application database details (APP_MONGO_DB, APP_MONGO_USER, APP_MONGO_PASSWORD) are not set in environment variables.");
    quit(1);
}

db = db.getSiblingDB(appDB);

db.createUser({
    user: appUser,
    pwd: appPassword,
    roles: [
        { role: "readWrite", db: appDB },
    ]
});
