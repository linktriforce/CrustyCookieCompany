CREATE TABLE WholesaleCustomer (
    customerID INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    address VARCHAR(255)
);

CREATE TABLE Orders (
    orderID INT AUTO_INCREMENT PRIMARY KEY,
    orderDate DATETIME,
    desiredDeliveryDate DATETIME,
    deliveryDate DATETIME,
    customerID INT,
    FOREIGN KEY (customerID) REFERENCES WholesaleCustomer(customerID)
);

CREATE TABLE Cookie (
    name VARCHAR(100) PRIMARY KEY
);

CREATE TABLE Pallets (
    palletID INT PRIMARY KEY AUTO_INCREMENT,
    isBlocked BOOLEAN,
    productionDate DATETIME,
    deliveredDate DATETIME,
    orderID INT,
    cookieName VARCHAR(100),
    FOREIGN KEY (orderID) REFERENCES Orders(orderID),
    FOREIGN KEY (cookieName) REFERENCES Cookie(name)
);

CREATE TABLE Ingredient (
    name VARCHAR(100) PRIMARY KEY,
    quantityTotal DECIMAL(10,2),
    unit VARCHAR(20),
    deliveredIntoStorage DATETIME,
    lastDeliveryQuantity DECIMAL(10,2)
);

CREATE TABLE CookieOrder (
    quantity INT,
    cookieName VARCHAR(100),
    orderID INT,
    PRIMARY KEY (cookieName, orderID),
    FOREIGN KEY (cookieName) REFERENCES Cookie(name),
    FOREIGN KEY (orderID) REFERENCES Orders(orderID)
);

CREATE TABLE Recipe (
    amount DECIMAL(10,2),
    cookieName VARCHAR(100),
    ingredientName VARCHAR(100),
    PRIMARY KEY (cookieName, ingredientName),
    FOREIGN KEY (cookieName) REFERENCES Cookie(name),
    FOREIGN KEY (ingredientName) REFERENCES Ingredient(name)
);
