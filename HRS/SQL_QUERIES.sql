
create table Customer (
id int primary key auto_increment,
name varchar(200) not null,
email varchar(200) not null unique,
age int);

create table Payment (
  id int primary key auto_increment,
  madeBy int not null,
  foreign key(madeBy) references Customer(id)
  on update cascade 
  on delete cascade,
  status enum('paid','partial','refund in progress','settled') not null,
  totaldues double not null,
  amountpaid double not null
 ); 
 
create table Room (
  id  int primary key auto_increment,
  type enum ('standard','premium','deluxe') not null,
  startdate date not null,
  enddate date not null,
  price double not null,
  status enum ('booked','available','deleted','check-in') not null
 ); 
 
 create table Bookingdetails (
  id  int primary key auto_increment,
  bookedBy int not null,
   foreign key(bookedBy) references Customer(id)
   on update cascade 
   on delete no action,
   roomId int not null,
   foreign key(roomId) references Room(id)
   on update cascade 
   on delete no action,
   paymentId int not null,
   foreign key(paymentId) references Payment(id)
   on update cascade 
   on delete no action,
   startdate date not null,
   enddate date not null
 );

create table Foodorder ( 
  id  int primary key auto_increment,
  orderedBy int not null,
  foreign key(orderedBy) references Customer(id)
  on update cascade
  on delete cascade 
);

create table FoodOrderDetails (
id int primary key auto_increment,
description varchar(200) not null,
foodOrder int not null,
foreign key(foodOrder) references Foodorder(id)
on update cascade
on delete cascade,
quantity int not null
);

create table Menu (
id int primary key auto_increment,
description varchar(200) not null unique
);