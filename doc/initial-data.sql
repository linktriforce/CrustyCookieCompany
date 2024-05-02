set foreign_key_checks = 0;
truncate table WholesaleCustomer;
truncate table Orders;
truncate table Pallets;
truncate table Ingredient;
truncate table Cookie;
truncate table CookieOrder;
truncate table Recipe;
set foreign_key_checks = 1;


-- Inserting data into WholesaleCustomer table
INSERT INTO WholesaleCustomer (name, address) VALUES
('Bjudkakor AB', 'Ystad'),
('Finkakor AB', 'Helsingborg'),
('Gästkakor AB', 'Hässleholm'),
('Kaffebröd AB', 'Landskrona'),
('Kalaskakor AB', 'Trelleborg'),
('Partykakor AB', 'Kristianstad'),
('Skånekakor AB', 'Perstorp'),
('Småbröd AB', 'Malmö');

-- Inserting data into Ingredient table
INSERT INTO Ingredient (name, quantityTotal, unit) VALUES
('Bread crumbs', 500000, 'g'),
('Butter', 500000, 'g'),
('Chocolate', 500000, 'g'),
('Chopped almonds', 500000, 'g'),
('Cinnamon', 500000, 'g'),
('Egg whites', 500000, 'ml'),
('Eggs', 500000, 'g'),
('Fine-ground nuts', 500000, 'g'),
('Flour', 500000, 'g'),
('Ground, roasted nuts', 500000, 'g'),
('Icing sugar', 500000, 'g'),
('Marzipan', 500000, 'g'),
('Potato starch', 500000, 'g'),
('Roasted, chopped nuts', 500000, 'g'),
('Sodium bicarbonate', 500000, 'g'),
('Sugar', 500000, 'g'),
('Vanilla sugar', 500000, 'g'),
('Vanilla', 500000, 'g'),
('Wheat flour', 500000, 'g');

-- Inserting data into Cookie table
INSERT INTO Cookie (name) VALUES
('Almond delight'),
('Amneris'),
('Berliner'),
('Nut cookie'),
('Nut ring'),
('Tango');

-- Inserting data into Recipe table
INSERT INTO Recipe (amount, cookieName, ingredientName) VALUES
(400, 'Almond delight', 'Butter'),
(279, 'Almond delight', 'Chopped almonds'),
(10, 'Almond delight', 'Cinnamon'),
(400, 'Almond delight', 'Flour'),
(270, 'Almond delight', 'Sugar'),
(250, 'Amneris', 'Butter'),
(250, 'Amneris', 'Eggs'),
(750, 'Amneris', 'Marzipan'),
(25, 'Amneris', 'Potato starch'),
(25, 'Amneris', 'Wheat flour'),
(250, 'Berliner', 'Butter'),
(50, 'Berliner', 'Chocolate'),
(50, 'Berliner', 'Eggs'),
(350, 'Berliner', 'Flour'),
(100, 'Berliner', 'Icing sugar'),
(5, 'Berliner', 'Vanilla sugar'),
(125, 'Nut cookie', 'Bread crumbs'),
(50, 'Nut cookie', 'Chocolate'),
(350, 'Nut cookie', 'Egg whites'),
(750, 'Nut cookie', 'Fine-ground nuts'),
(625, 'Nut cookie', 'Ground, roasted nuts'),
(375, 'Nut cookie', 'Sugar'),
(450, 'Nut ring', 'Butter'),
(450, 'Nut ring', 'Flour'),
(190, 'Nut ring', 'Icing sugar'),
(225, 'Nut ring', 'Roasted, chopped nuts'),
(200, 'Tango', 'Butter'),
(300, 'Tango', 'Flour'),
(4, 'Tango', 'Sodium bicarbonate'),
(250, 'Tango', 'Sugar'),
(2, 'Tango', 'Vanilla');