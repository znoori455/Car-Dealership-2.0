create TABLE Customer
(
    CustomerId serial NOT NULL,
    Username VARCHAR(50) NOT NULL,
    Pass VARCHAR(50) NOT NULL,
    CONSTRAINT PK_Customer PRIMARY KEY  (CustomerId)
);
--drop table Customer cascade;

--drop table employee cascade;
create TABLE Employee
(
    EmployeeId serial NOT NULL,
    Username VARCHAR(50) NOT NULL,
    Pass VARCHAR(50) NOT NULL,
    CONSTRAINT PK_Employee PRIMARY KEY  (EmployeeId)
);
insert into Employee (Username, pass) values ('a', 'a');


--drop table car cascade;
create TABLE Car
(
    CarId serial NOT NULL,
    make_model VARCHAR(50) NOT NULL,
    mileage VARCHAR(10) NOT NULL,
    year VARCHAR(4) NOT NULL,
    price numeric(10,2),
    monthly_payment numeric(10, 2),
    OwnerID int,
    SellerID int,
    CONSTRAINT PK_Car PRIMARY KEY  (CarID)
);
ALTER TABLE car ADD active char(1) NOT NULL;
ALTER TABLE Car ADD CONSTRAINT FK_Owner
    FOREIGN KEY (OwnerID) REFERENCES Customer (CustomerId) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE Car ADD CONSTRAINT FK_Seller
    FOREIGN KEY (SellerID) REFERENCES Employee (EmployeeID) ON DELETE NO ACTION ON UPDATE NO ACTION;

insert into car (make_model, mileage, year, active) values ('Mystery Machine', '79365', '1964', 't');  
insert into car (make_model, mileage, year, active) values ('Batmobile', '24899', '2008', 't');  
insert into car (make_model, mileage, year, active) values ('Invisible Boatmobile', '4533', '1999', 't');  
insert into car (make_model, mileage, year, active) values ('Magic School Bus', '97343', '1994', 't');  
insert into car (make_model, mileage, year, active) values ('Millenium Falcon', '532948399', '????', 't');  
insert into car (make_model, mileage, year, active) values ('Homer''s Pink Sedan', '147498', '1989', 't');  
insert into car (make_model, mileage, year, active) values ('Bebop', '987382743', '2077', 't');  

   
--drop table status cascade;   
create TABLE Status
(
   	StatusID serial not null,
   	Status varchar(10),
   	CONSTRAINT PK_StatusID PRIMARY KEY  (StatusID)
);
insert into Status (status) values ('Pending');
insert into Status (status) values ('Accepted');
insert into Status (status) values ('Rejected');   
   

--drop table offer;
create TABLE Offer
(
	OfferID serial not null,
    offerprice numeric(10,2) not null,
    approval_date timestamp,
    CarID int not null,
    CustomerID int not null,
    StatusID int not null,
    CONSTRAINT PK_OfferID PRIMARY KEY  (OfferID)
);
ALTER TABLE Offer ADD CONSTRAINT FK_Car
    FOREIGN KEY (CarID) REFERENCES Car (CarID) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE Offer ADD CONSTRAINT FK_Customer
    FOREIGN KEY (CustomerID) REFERENCES Customer (CustomerID) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE Offer ADD CONSTRAINT FK_Status
    FOREIGN KEY (StatusID) REFERENCES Status (StatusID) ON DELETE NO ACTION ON UPDATE NO ACTION;


   
create TABLE Payment
(
	PaymentID serial not null,
	CarID int not null,
    amount numeric(10, 2) not null,
    date timestamp not null,
    CONSTRAINT PK_Payment PRIMARY KEY  (PaymentId)
);
--drop table payment;

ALTER TABLE Payment ADD CONSTRAINT FK_Car
    FOREIGN KEY (CarID) REFERENCES Car (CarID) ON DELETE NO ACTION ON UPDATE NO ACTION;
   
   
-----------------------------------------------------------Functions---------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------

create or replace function newCustomer(in u varchar, in p varchar)
returns void as $$
begin 
	INSERT INTO customer (username, pass) VALUES ($1, $2);
end;
$$ language plpgsql;

create or replace function newEmployee(in u varchar, in p varchar)
returns void as $$
begin 
	INSERT INTO Employee (username, pass) VALUES ($1, $2);
end;
$$ language plpgsql;




create or replace function newCar(in mm varchar, in miles varchar, in age varchar)
returns int as $$
declare
	ID integer;
begin 
	INSERT INTO Car (make_model, mileage, year, active) VALUES ($1, $2, $3, 't');
	select max(carid) into ID from car;
	return ID;
end;
$$ language plpgsql;



create or replace function newOffer(in custname varchar, in carID integer, in offer double precision)
returns void as $$
declare
	cust integer;
begin 
	select customerid into cust from customer where username = $1;
	INSERT INTO offer (offerprice, carid, customerid, statusid) VALUES ($3, $2, cust, 1);
end;
$$ language plpgsql;

create or replace function getowned(in custname varchar)
returns setof record as $$
begin 
	select * from car where ownerid = (select customerID from customer where username = $1);
end;
$$ language plpgsql;





create or replace function makePayment(in car integer, in paying double precision)
returns double precision as $$
declare 
paymentsmade double precision;
begin
	INSERT INTO payment (carid, amount, date) VALUES ($1, $2, (select now()));
	select SUM(amount) into paymentsmade from payment where carid = $1;
	return paymentsmade;
end;
$$ language plpgsql;



create or replace function approveoffer(in approvedcar int, in cust varchar, in emp varchar)
returns void as $$
declare 
custID integer;
empID integer;
amount double precision;
begin
	select customerid into custID from customer where username = $2;
	select employeeid into empID from employee where username = $3;
	update offer set approval_date = (select now()), statusid = 2 where customerid = custID;
	update offer set statusid = 3 where statusid != 2;
	select offerprice into amount from offer where statusid = 2 and customerid = custID;
	update car set price = amount, ownerid = custID, sellerid = empID, active = 'f' where carid = $1;
end;
$$ language plpgsql;



create or replace function set_monthly(in ID int, in monthly double precision)
returns void as $$
begin
	update car set monthly_payment = $2 where carid = $1;
end;
$$ language plpgsql;