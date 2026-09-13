import initSqlJs from 'sql.js';

let SQL = null;

const databases = new Map();
const statements = new Map();

let nextDatabaseId = 0;
let nextStatementId = 0;

function openRequest(id, requestData) {
    try {
        const newDatabaseId = nextDatabaseId++;
        const newDatabase = new SQL.Database();
        databases.set(newDatabaseId, newDatabase);
        postMessage({'id': id, data: {'databaseId': newDatabaseId}});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function prepareRequest(id, requestData) {
    try {
        const newStatementId = nextStatementId++;
        const database = databases.get(requestData.databaseId);
        if (!database) {
            postMessage({'id': id, error: "Invalid database ID: " + requestData.databaseId});
            return;
        }
        const statement = database.prepare(requestData.sql);
        statements.set(newStatementId, statement);
        const resultData = {
            'statementId': newStatementId,
            'parameterCount': 256,
            'columnNames': statement.getColumnNames()
        };
        postMessage({'id': id, data: resultData});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function stepRequest(id, requestData) {
    const statement = statements.get(requestData.statementId);
    if (!statement) {
        postMessage({'id': id, error: "Invalid statement ID: " + requestData.statementId});
        return;
    }
    try {
        const resultData = {
            'rows': [],
            'columnTypes': []
        };
        statement.reset();
        if (requestData.bindings && requestData.bindings.length > 0) {
            statement.bind(requestData.bindings);
        }
        while (statement.step()) {
            const row = statement.get();
            if (resultData.columnTypes.length === 0) {
                for (let i = 0; i < row.length; i++) {
                    const val = row[i];
                    if (val === null) {
                        resultData.columnTypes.push(5);
                    } else if (typeof val === 'number') {
                        resultData.columnTypes.push(Number.isInteger(val) ? 1 : 2);
                    } else if (typeof val === 'string') {
                        resultData.columnTypes.push(3);
                    } else if (val instanceof Uint8Array) {
                        resultData.columnTypes.push(4);
                    } else {
                        resultData.columnTypes.push(5);
                    }
                }
            }
            resultData.rows.push(row);
        }
        postMessage({'id': id, data: resultData});
    } catch (error) {
        postMessage({'id': id, error: error.message});
    }
}

function closeRequest(id, requestData) {
    if (requestData.statementId !== undefined && requestData.statementId != null) {
        const statement = statements.get(requestData.statementId);
        if (statement == null) {
            postMessage({'id': id, error: "Invalid statement ID: " + requestData.statementId});
            return;
        }
        try {
            statement.free();
            statements.delete(requestData.statementId);
        } catch (error) {
            postMessage({'id': id, error: error.message});
        }
    }
    if (requestData.databaseId !== undefined && requestData.databaseId != null) {
        const database = databases.get(requestData.databaseId);
        if (database == null) {
            postMessage({'id': id, error: "Invalid database ID: " + requestData.databaseId});
            return;
        }
        try {
            database.close();
            databases.delete(requestData.databaseId);
        } catch (error) {
            postMessage({'id': id, error: error.message});
        }
    }
}

const commandMap = {
    'open': openRequest,
    'prepare': prepareRequest,
    'step': stepRequest,
    'close': closeRequest,
};

function handleMessage(e) {
    const requestMsg = e.data;
    if (!Object.hasOwn(requestMsg, 'data') && requestMsg.data == null) {
        postMessage({'id': requestMsg.id, 'error': "Invalid request, missing 'data'."});
        return;
    }
    if (!Object.hasOwn(requestMsg.data, 'cmd') && requestMsg.data.cmd == null) {
        postMessage({'id': requestMsg.id, 'error': "Invalid request, missing 'cmd'."});
        return;
    }
    const command = requestMsg.data.cmd;
    const requestHandler = commandMap[command];
    if (requestHandler) {
        requestHandler(requestMsg.id, requestMsg.data);
    } else {
        postMessage({'id': requestMsg.id, 'error': "Invalid request, unknown command: '" + command + "'."});
    }
}

const messageQueue = [];
onmessage = (e) => {
    if (!SQL) {
        messageQueue.push(e);
    } else {
        handleMessage(e);
    }
};

initSqlJs({
    locateFile: file => 'https://cdnjs.cloudflare.com/ajax/libs/sql.js/1.13.0/sql-wasm.wasm'
}).then(instance => {
    SQL = instance;
    while (messageQueue.length > 0) {
        handleMessage(messageQueue.shift());
    }
});
