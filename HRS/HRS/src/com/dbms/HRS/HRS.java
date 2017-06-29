package com.dbms.HRS;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;

public class HRS {
	private List<Room>				availableRoomList		= new ArrayList<Room>();
	private List<Room>				hotelRoomList			= new ArrayList<Room>();
	private List<Customer>			customerList			= new ArrayList<Customer>();
	private List<BookingDetails>	bookingDetailsList		= new ArrayList<BookingDetails>();
	private List<BookingDetails>	allbookingDetailsList	= new ArrayList<BookingDetails>();
	@SuppressWarnings("unused")
	private List<Payment>			payment					= new ArrayList<Payment>();
	private List<Menu>				menuList				= new ArrayList<Menu>();

	/**
	 * The database connection.
	 */
	private static Connection		connection;

	public static Connection JDBC_Connection(String driver, String url,
			String user, String password) {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			System.err.println("Unable to connect to the database due to " + e);
		}
		return connection;
	}

	/* The method to check for SQL Warning */
	private void checkForWarnings(SQLWarning warning, String message)
			throws Exception {

		// If there are no warnings then just return.

		if (warning == null) {
			return;
		}

		// Concatenate all of the warnings, one per line.

		String concatenation = "";
		while (warning != null) {
			concatenation = "\n  " + warning;
			warning = warning.getNextWarning();

		}
		throw new Exception("Warning(s) occurred while " + message + " for HRS"
				+ concatenation);
	}

	/**
	 * This method is invoked when HRS admin want to add a new Room in HRS
	 * 
	 * @param connection
	 * @param roomType
	 * @param roomprice
	 * @throws SQLException
	 */
	public void addRoom(Connection connection, String roomType,
			float roomprice) throws SQLException

	{
		String newRoom = "insert into Room (type,startdate,enddate,price,status) VALUES (?,?,?,?,?)";
		PreparedStatement addRoom = connection.prepareStatement(newRoom,
				java.sql.Statement.RETURN_GENERATED_KEYS);
		addRoom.setString(1, roomType);
		java.sql.Timestamp date = new java.sql.Timestamp(
				new java.util.Date().getTime());
		addRoom.setTimestamp(2, date);
		addRoom.setDate(3, Date.valueOf("2017-12-31"));
		addRoom.setFloat(4, roomprice);
		addRoom.setString(5, "available");
		int addRoomcount = addRoom.executeUpdate();

		if (addRoomcount == 0)
		{
			System.out
					.println("New room record cannot be created at this time.Please try later");

		}

		else
		{
			ResultSet keys = addRoom.getGeneratedKeys();
			keys.next();
			int roomId = keys.getInt(1);
			System.out.println("The roomid of the new room is =>" + roomId);
			DBTablePrinter.printTable(connection, "Room");
			keys.close();
		}

		addRoom.close();
	}

	/**
	 * This method is invoked when HRS admin wants to delete a remove from HRS
	 * 
	 * @param connection
	 * @param roomid
	 * @throws Exception
	 */
	public void deleteRoom(Connection connection, int roomid)
			throws Exception

	{
		PreparedStatement roomId = connection
				.prepareStatement("select r.id from Room r "
						+ "where r.id =?");
		roomId.setInt(1, roomid);
		checkForWarnings(roomId.getWarnings(), "fetching the room-id");
		ResultSet room = roomId.executeQuery();
		if (!room.first())
		{
			throw new SQLException("There is no room with this id => "
					+ roomid + "  " + "in the HRS");

		}

		else
		/** check whether the current status of the room **/
		{
			String selectRoomstatus = "select r.status from Room r where r.id=?";
			PreparedStatement roomstatus = connection
					.prepareStatement(selectRoomstatus);
			roomstatus.setInt(1, roomid);
			ResultSet roomstat = roomstatus.executeQuery();
			roomstat.next();
			String rstatus = roomstat.getString(1);
			roomstat.close();

			if (rstatus.equalsIgnoreCase("available"))

			{

				DBTablePrinter.printTable(connection, "Room");
				String delRoom = "update Room r set r.status=? where r.id=?";
				PreparedStatement deleteRoom = connection
						.prepareStatement(delRoom);
				deleteRoom.setString(1, "deleted");
				deleteRoom.setInt(2, roomid);
				int deleteRoomcount = deleteRoom.executeUpdate();
				System.out.println("Number of rooms  deleted is =>"
						+ deleteRoomcount);
				DBTablePrinter.printTable(connection, "Room");
				deleteRoom.close();

			}

			else

				throw new Exception("Room is not available for deletion");

		}

	}

	/**
	 * This method is invoked when HRS admin creates a new customer in HRS
	 * 
	 * @param connection
	 * @param name
	 * @param email
	 * @param age
	 * @throws SQLException
	 */

	public void addCustomer(Connection connection, String name, String email,
			int age) throws SQLException

	{
		String newCustomer = "insert ignore into Customer (name,email,age) VALUES (?,?,?)";
		PreparedStatement addCustomer = connection.prepareStatement(
				newCustomer, Statement.RETURN_GENERATED_KEYS);
		addCustomer.setString(1, name);
		addCustomer.setString(2, email);
		addCustomer.setInt(3, age);
		int addCount = addCustomer.executeUpdate();
		if (addCount == 0)
			System.out
					.println("Customer with same email address already exists in HRS");

		else
		{
			ResultSet keys = addCustomer.getGeneratedKeys();
			keys.next();
			int custId = keys.getInt(1);
			System.out.println("The custid of the new customer is =>" + custId);
			DBTablePrinter.printTable(connection, "Customer");
			keys.close();
		}
		addCustomer.close();

	}

	/**
	 * This method is invoked when HRS wants to view all the customers of HRS
	 * 
	 * @param connection
	 * @throws SQLException
	 */

	public void searchallCustomer(Connection connection) throws Exception

	{
		String allCustomer = "select * from Customer";
		PreparedStatement searchCustomer = connection
				.prepareStatement(allCustomer);
		checkForWarnings(searchCustomer.getWarnings(),
				"fetching the customer details");
		ResultSet rs = searchCustomer.executeQuery();
		while (rs.next()) {
			checkForWarnings(searchCustomer.getWarnings(),
					"setting the customer details");
			Customer customer = new Customer();
			customer.setId(rs.getInt(1));
			customer.setName(rs.getString(2));
			customer.setEmail(rs.getString(3));
			customer.setAge(rs.getInt(4));
			customerList.add(customer);
		}

		DBTablePrinter.printTable(connection, "Customer");
		rs.close();

	}

	/**
	 * 
	 */

	public void allBookingDetails(Connection connection) throws Exception

	{
		String allBookings = "select * from Bookingdetails";
		PreparedStatement searchCustomer = connection
				.prepareStatement(allBookings);
		checkForWarnings(searchCustomer.getWarnings(),
				"fetching the booking details");
		ResultSet rs = searchCustomer.executeQuery();
		while (rs.next()) {
			checkForWarnings(searchCustomer.getWarnings(),
					"setting the booking details");
			BookingDetails bd = new BookingDetails();
			bd.setBookingId(rs.getInt(1));
			bd.setBookedBy(rs.getInt(2));
			bd.setRoomId(rs.getInt(3));
			bd.setPaymentId(rs.getInt(4));
			bd.setStartdate(rs.getDate(5));
			bd.setEnddate(rs.getDate(6));
			allbookingDetailsList.add(bd);
		}

		DBTablePrinter.printTable(connection, "Bookingdetails");
		rs.close();

	}

	/**
	 * This method is invoked by HRS admin to see all the rooms with their
	 * current status at any given instance
	 * 
	 * @param connection
	 * @throws SQLException
	 */

	public void allRooms(Connection connection) throws Exception

	{
		String allroomSearch = "select * from Room ";
		PreparedStatement allroom = connection.prepareStatement(allroomSearch);
		checkForWarnings(allroom.getWarnings(), "fetching all the room details");
		ResultSet rs = allroom.executeQuery();
		while (rs.next())
		{

			checkForWarnings(allroom.getWarnings(), "setting the room details");
			Room room = new Room();
			room.setId(rs.getInt(1));
			room.setRoomType(rs.getString(2));
			room.setStartDate(rs.getDate(3));
			room.setEndDate(rs.getDate(4));
			room.setPrice(rs.getFloat(5));
			room.setStatus(rs.getString(6));
			hotelRoomList.add(room);

		}

		DBTablePrinter.printTable(connection, "Room");
		rs.close();

	}

	/**
	 * This method is used by a potential customer to see the available rooms in
	 * hotel
	 * 
	 * @param connection
	 * @param startDate
	 * @param endDate
	 * @param roomType
	 * @throws SQLException
	 */

	public void searchRoom(Connection connection, Date startDate, Date endDate,
			String roomType) throws Exception {
		String roomSearch = "select id, type, startDate, endDate, price, status from Room r where r.status=? and r.type=? and r.startdate <= ? AND r.enddate >= ?";
		PreparedStatement roomSearchObject = connection
				.prepareStatement(roomSearch);
		roomSearchObject.setString(1, "available");
		roomSearchObject.setString(2, roomType);
		roomSearchObject.setDate(3, startDate);
		roomSearchObject.setDate(4, endDate);
		checkForWarnings(roomSearchObject.getWarnings(),
				"fetching the room details based on conditions");
		ResultSet rs = roomSearchObject.executeQuery();

		while (rs.next()) {

			checkForWarnings(roomSearchObject.getWarnings(),
					"setting the room details");
			Room room = new Room();
			room.setId(rs.getInt(1));
			room.setRoomType(rs.getString(2));
			room.setStartDate(rs.getDate(3));
			room.setEndDate(rs.getDate(4));
			room.setPrice(rs.getFloat(5));
			room.setStatus(rs.getString(6));
			availableRoomList.add(room);
		}

		rs.close();

		for (Room room : availableRoomList) {
			System.out.println(room.getId() + " " + room.getRoomType() + " "
					+ room.getStartDate() + " "
					+ room.getEndDate() + " " + room.getPrice() + " "
					+ room.getStatus());

		}

	}

	/**
	 * This method is invoked when a customer makes a payment for Room
	 * reservation
	 * 
	 * @param connection
	 * @param custId
	 * @param payment
	 * @param roomprice
	 * @param stayDuration
	 * @return
	 * @throws SQLException
	 */
	public int makePayment(Connection connection, int custId, float payment,
			float roomprice, long stayDuration) throws SQLException {
		String newPayment = "insert into Payment (madeBy,status,totaldues,amountpaid) VALUES (?,?,?,?)";
		PreparedStatement addPayment = connection.prepareStatement(newPayment,
				Statement.RETURN_GENERATED_KEYS);
		addPayment.setInt(1, custId);
		float amountToBePaid = stayDuration * roomprice;
		if (payment < amountToBePaid) {
			addPayment.setString(2, "partial");
			addPayment.setFloat(3, (amountToBePaid - payment));
		}
		else if (payment == amountToBePaid) {
			addPayment.setString(2, "paid");
			addPayment.setFloat(3, 0);
		}

		addPayment.setFloat(4, payment);
		addPayment.executeUpdate();
		ResultSet keys = addPayment.getGeneratedKeys();
		keys.next();
		int paymentId = keys.getInt(1);
		keys.close();
		return paymentId;
	}

	/**
	 * This method is invoked when customers wants to book a room
	 * 
	 * @param connection
	 * @param custid
	 * @param startDate
	 * @param endDate
	 * @param roomType
	 * @param payment
	 * @throws Exception
	 */

	public void bookRoom(Connection connection, int custid, Date startDate,
			Date endDate, String roomType, float payment)
			throws Exception {

		PreparedStatement validId = connection
				.prepareStatement("select c.id from Customer c "
						+ "where c.id =?");
		validId.setInt(1, custid);
		checkForWarnings(validId.getWarnings(), "fetching the customer-id");
		ResultSet custId = validId.executeQuery();
		if (!custId.first())
		{
			throw new SQLException("There is no customer with this id => "
					+ custid + "  " + "in the HRS");

		}

		else
		{
			System.out
					.println("The current status of the rooms in HRS are as below");
			DBTablePrinter.printTable(connection, "Room");
			long stayDuration = calculateNoOfDays(startDate, endDate);
			String selectRoom = "select r.id,r.price from room r where r.type=? and r.status=? and r.startdate <= ? and r.enddate >= ?";
			PreparedStatement getRoom = connection.prepareStatement(selectRoom);
			getRoom.setString(1, roomType);
			getRoom.setString(2, "available");
			getRoom.setDate(3, startDate);
			getRoom.setDate(4, endDate);
			checkForWarnings(getRoom.getWarnings(),
					"fetching the room details based on type and status");
			ResultSet rs = getRoom.executeQuery();
			rs.first();
			if (!rs.first())
			{
				throw new Exception(
						"There is no room availble now for booking for roomtype=> "
								+ roomType + " "
								+ ".Please modify your search criteria");
			}

			else
			{
				int roomid = rs.getInt(1);
				float roomprice = rs.getFloat(2);
				int paymentId = makePayment(connection, custid, payment,
						roomprice,
						stayDuration);
				String bookRoom = "insert into Bookingdetails (bookedBy,roomId,paymentId,startdate,enddate) VALUES (?,?,?,?,?)";
				PreparedStatement bookDetails = connection.prepareStatement(
						bookRoom, Statement.RETURN_GENERATED_KEYS);
				bookDetails.setInt(1, custid);
				bookDetails.setInt(2, roomid);
				bookDetails.setInt(3, paymentId);
				bookDetails.setDate(4, startDate);
				bookDetails.setDate(5, endDate);
				int bookingCount = bookDetails.executeUpdate();
				ResultSet keys = bookDetails.getGeneratedKeys();
				keys.next();
				int bookingId = keys.getInt(1);
				keys.close();

				if (bookingCount == 1)
				{
					String updateRoom = "update Room r set r.status=?, r.startdate=?,r.enddate=? where r.id=?";
					PreparedStatement statusUpdate = connection
							.prepareStatement(updateRoom);
					statusUpdate.setString(1, "booked");
					statusUpdate.setDate(2, startDate);
					statusUpdate.setDate(3, endDate);
					statusUpdate.setInt(4, roomid);
					int updateroom = statusUpdate.executeUpdate();
					System.out.println("The alloted roomid => " + roomid);
					System.out.println("The paymentid for booking is => "
							+ paymentId);
					System.out.println("The booking-id for this booking => "
							+ bookingId);
					DBTablePrinter.printTable(connection, "Room");
					statusUpdate.close();
				}

				else

					throw new Exception(
							"Booking not succesful,please check with HRS administrator");

			}

			rs.close();

		}

	}

	/**
	 * This method is invoked when a customer cancels an existing booking
	 * 
	 * @param connection
	 * @param custid
	 * @param bookingid
	 * @throws SQLException
	 */

	public void cancelBooking(Connection connection, int custid, int bookingid)
			throws Exception

	{

		PreparedStatement validId = connection
				.prepareStatement("select c.id from Customer c "
						+ "where c.id =?");
		validId.setInt(1, custid);
		checkForWarnings(validId.getWarnings(), "fetching the customer-id");
		ResultSet custId = validId.executeQuery();
		if (!custId.first())
		{
			throw new SQLException("There is no customer with this id => "
					+ custid + "  " + "in the HRS");

		}

		else {
			PreparedStatement validId1 = connection
					.prepareStatement("select b.id from Bookingdetails b "
							+ "where b.id =?");
			validId1.setInt(1, bookingid);
			checkForWarnings(validId1.getWarnings(), "fetching the booking-id");
			ResultSet bookid = validId1.executeQuery();
			if (!bookid.first())
			{
				throw new SQLException("There is no booking with this id => "
						+ bookingid + "  " + "in the HRS");

			}

			else
			{

				String selectBooking = "select b.paymentId,b.roomId from Bookingdetails b where b.id=? and b.bookedBy=?";
				PreparedStatement select = connection
						.prepareStatement(selectBooking);
				select.setInt(1, bookingid);
				select.setInt(2, custid);
				checkForWarnings(select.getWarnings(),
						"fetching the booking details");
				ResultSet rs = select.executeQuery();
				rs.next();
				int roomId = rs.getInt(2);
				int paymentId = rs.getInt(1);
				rs.close();

				if (roomId == 0)
				{
					System.out
							.println("No booking available with the given booking-id.Please check with HRS administrator");

				}

				else

				{

					/** check whether the current status of the room **/

					String selectRoomstatus = "select r.status from Room r where r.id=?";
					PreparedStatement roomstatus = connection
							.prepareStatement(selectRoomstatus);
					roomstatus.setInt(1, roomId);
					ResultSet roomstat = roomstatus.executeQuery();
					roomstat.next();
					String rstatus = roomstat.getString(1);
					roomstat.close();

					if (rstatus.equalsIgnoreCase("booked"))

					{

						/** Delete the entry from Bookingdetails table **/
						String cancelBooking = "delete from Bookingdetails where id=? and bookedBy=?";
						PreparedStatement cancel = connection
								.prepareStatement(cancelBooking);
						cancel.setInt(1, bookingid);
						cancel.setInt(2, custid);
						cancel.executeUpdate();
						DBTablePrinter.printTable(connection, "Bookingdetails");
						cancel.close();

						/** Update the rooms table **/
						String updateRoom = "update Room r set r.status=?,r.startdate=?,r.enddate=? where r.id=?";
						PreparedStatement ps = connection
								.prepareStatement(updateRoom);
						java.sql.Timestamp date = new java.sql.Timestamp(
								new java.util.Date().getTime());
						ps.setString(1, "available");
						ps.setTimestamp(2, date);
						ps.setDate(3, Date.valueOf("2017-12-31"));
						ps.setInt(4, roomId);
						ps.executeUpdate();
						DBTablePrinter.printTable(connection, "Room");
						ps.close();

						/** Update the payment table **/
						String updatePayment = "update Payment p set p.status=?,p.totaldues=? where p.id=?";
						PreparedStatement ps1 = connection
								.prepareStatement(updatePayment);
						ps1.setString(1, "refund in progress");
						ps1.setFloat(2, 0);
						ps1.setInt(3, paymentId);
						ps1.executeUpdate();
						DBTablePrinter.printTable(connection, "Payment");
						ps1.close();

					}

					else

						throw new Exception(
								"Booking cannot be cancelled, please check with HRS administrator");

				}

			}

		}
	}

	/**
	 * This method is invoked when a customer wants to view his/her booking
	 * details
	 * 
	 * @param connection
	 * @param custid
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	private void bookingDetails(Connection connection, int custid, int bookingid)
			throws Exception
	{

		PreparedStatement validId1 = connection
				.prepareStatement("select c.id from Customer c "
						+ "where c.id =?");
		validId1.setInt(1, custid);
		checkForWarnings(validId1.getWarnings(), "fetching the customer-id");
		ResultSet custId = validId1.executeQuery();
		if (!custId.first())
		{
			throw new SQLException("There is no customer with this id => "
					+ custid + "  " + "in the HRS");

		}

		else {

			PreparedStatement validId = connection
					.prepareStatement("select b.id from Bookingdetails b "
							+ "where b.id =?");
			validId.setInt(1, bookingid);
			checkForWarnings(validId.getWarnings(), "fetching the booking-id");
			ResultSet bookid = validId.executeQuery();
			if (!bookid.first())
			{
				throw new SQLException("There is no booking with this id => "
						+ bookingid + "  " + "in the HRS");

			}

			else
			{

				String bookingDetail = "select * from Bookingdetails b where b.bookedBy=? and b.id=? order by b.id desc";
				PreparedStatement pstmt = connection
						.prepareStatement(bookingDetail);
				pstmt.setInt(1, custid);
				pstmt.setInt(2, bookingid);
				checkForWarnings(pstmt.getWarnings(),
						"fetching the booking details based on the custId");
				ResultSet rs = pstmt.executeQuery();
				DBTablePrinter.printResultSet(rs);
				rs.first();
				int paymentID=rs.getInt(4);
				String paymentDetail = "select * from Payment p where p.id=?";
				PreparedStatement pstmt1 = connection
						.prepareStatement(paymentDetail);
				pstmt1.setInt(1, paymentID);
				checkForWarnings(pstmt1.getWarnings(),
						"fetching the payment details based on the paymentId");
				ResultSet rs1 = pstmt1.executeQuery();
				DBTablePrinter.printResultSet(rs1);
				pstmt1.close();
				pstmt.close();

			}

		}
	}

	/**
	 * This method is invoked to calculate the duration of stay of customer
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException
	 */
	private long calculateNoOfDays(Date startDate, Date endDate)
			throws ParseException {
		long noOfDays = 0;
		java.util.Date utilStartDate = new java.util.Date(startDate.getTime());
		java.util.Date utilEndDate = new java.util.Date(endDate.getTime());
		noOfDays = ChronoUnit.DAYS.between(utilStartDate.toInstant(),
				utilEndDate.toInstant());
		return noOfDays;
	}

	/**
	 * This method is invoked by HRS admin to update the room status when a
	 * customer checks-in
	 * 
	 * @param connection
	 * @param custid
	 * @param bookingid
	 * @throws SQLException
	 */
	public void checkIn(Connection connection, int custid, int bookingid)
			throws Exception
	{

		PreparedStatement validId1 = connection
				.prepareStatement("select c.id from Customer c "
						+ "where c.id =?");
		validId1.setInt(1, custid);
		checkForWarnings(validId1.getWarnings(), "fetching the customer-id");
		ResultSet custId = validId1.executeQuery();
		if (!custId.first())
		{
			throw new SQLException("There is no customer with this id => "
					+ custid + "  " + "in the HRS");

		}

		else {
			PreparedStatement validId = connection
					.prepareStatement("select b.id from Bookingdetails b "
							+ "where b.id =?");
			validId.setInt(1, bookingid);
			checkForWarnings(validId.getWarnings(), "fetching the booking-id");
			ResultSet bookid = validId.executeQuery();
			if (!bookid.first())
			{
				throw new SQLException("There is no booking with this id => "
						+ bookingid + "  " + "in the HRS");

			}

			else
			{

				/** Select the room booked by the customer **/
				String booking = "select b.roomId from Bookingdetails b where b.id=? and b.bookedBy=?";
				PreparedStatement select = connection.prepareStatement(booking);
				select.setInt(1, bookingid);
				select.setInt(2, custid);
				checkForWarnings(select.getWarnings(),
						"fetching the roomId based on the custId and the bookig ID");
				ResultSet rs = select.executeQuery();
				rs.next();
				int roomId = rs.getInt(1);
				// System.out.println(roomId);
				String selectRoomstatus = "select r.status from Room r where r.id=?";
				PreparedStatement roomstatus = connection
						.prepareStatement(selectRoomstatus);
				roomstatus.setInt(1, roomId);
				ResultSet roomstat = roomstatus.executeQuery();
				roomstat.next();
				String rstatus = roomstat.getString(1);
				// System.out.println(rstatus);

				if (rstatus.equalsIgnoreCase("booked"))

				{

					/** Update the room-status to check-in **/
					String updateRoom = "update Room r set r.status=? where r.id=?";
					PreparedStatement ps = connection
							.prepareStatement(updateRoom);
					ps.setString(1, "check-in");
					ps.setInt(2, roomId);
					ps.executeUpdate();
					DBTablePrinter.printTable(connection, "Room");
					ps.close();

				}

				else

				{

					throw new Exception(
							"Check-in is not succesful, please check with HRS administrator");

				}

			}

		}

	}

	/**
	 * This method is invoked when a customer checks-out after settling all the
	 * bills at reception
	 * 
	 * @param connection
	 * @param custid
	 * @param bookingid
	 * @throws SQLException
	 */

	public void checkOut(Connection connection, int custid, int bookingid)
			throws Exception
	{
		PreparedStatement validId1 = connection
				.prepareStatement("select c.id from Customer c "
						+ "where c.id =?");
		validId1.setInt(1, custid);
		checkForWarnings(validId1.getWarnings(), "fetching the customer-id");
		ResultSet custId = validId1.executeQuery();
		if (!custId.first())
		{
			throw new SQLException("There is no customer with this id => "
					+ custid + "  " + "in the HRS");

		}

		else {
			PreparedStatement validId = connection
					.prepareStatement("select b.id from Bookingdetails b "
							+ "where b.id =?");
			validId.setInt(1, bookingid);
			checkForWarnings(validId.getWarnings(), "fetching the booking-id");
			ResultSet bookid = validId.executeQuery();
			if (!bookid.first())
			{
				throw new SQLException("There is no booking with this id => "
						+ bookingid + "  " + "in the HRS");

			}

			else
			{
				String booking = "select b.roomId,b.paymentId from bookingdetails b where b.id=? and b.bookedBy=?";
				PreparedStatement select = connection.prepareStatement(booking);
				select.setInt(1, bookingid);
				select.setInt(2, custid);
				checkForWarnings(select.getWarnings(),
						"fetching the roomId and paymentId based on the custId and the bookig ID");
				ResultSet rs = select.executeQuery();
				rs.next();

				int roomId = rs.getInt(1);
				int paymentId = rs.getInt(2);

				if (roomId == 0)

				{
					System.out
							.println("There is no romm for the given room id");

				}

				else

				{

					String selectRoomstatus = "select r.status from Room r where r.id=?";
					PreparedStatement roomstatus = connection
							.prepareStatement(selectRoomstatus);
					roomstatus.setInt(1, roomId);
					ResultSet roomstat = roomstatus.executeQuery();
					roomstat.next();
					String rstatus = roomstat.getString(1);
					roomstat.close();

					if (rstatus.equalsIgnoreCase("check-in"))

					{
						/** update the room table **/

						String updateRoom = "update Room r set r.status=?, r.startdate=?,r.enddate=? where r.id=?";
						PreparedStatement ps = connection
								.prepareStatement(updateRoom);
						java.sql.Timestamp date = new java.sql.Timestamp(
								new java.util.Date().getTime());
						ps.setString(1, "available");
						ps.setTimestamp(2, date);
						ps.setDate(3, Date.valueOf("2017-12-31"));
						ps.setInt(4, roomId);
						ps.executeUpdate();
						System.out.println("Customer =>" + " " + custid + " "
								+ "checked out from room =>" + roomId);
						DBTablePrinter.printTable(connection, "Room");

						/** update the payment table **/

						String selectPayment = "select p.amountpaid,p.totaldues from Payment p where p.id=?";
						PreparedStatement ps1 = connection
								.prepareStatement(selectPayment);
						ps1.setInt(1, paymentId);
						ResultSet rs1 = ps1.executeQuery();
						rs1.next();
						float amountpaid = rs1.getFloat(1);
						float totaldues = rs1.getFloat(2);
						float fullPayment = amountpaid + totaldues;
						String updatePayment = "update Payment p set p.status=?,p.totaldues=?, p.amountpaid=? where p.id=?";
						PreparedStatement ps2 = connection
								.prepareStatement(updatePayment);
						ps2.setString(1, "settled");
						ps2.setFloat(2, 0);
						ps2.setFloat(3, fullPayment);
						ps2.setInt(4, paymentId);
						ps2.executeUpdate();
						System.out.println("Bill settled for paymentid =>"
								+ paymentId);
						DBTablePrinter.printTable(connection, "Payment");
						rs.close();
						rs1.close();
						ps.close();
						ps1.close();
						ps2.close();

					}

					else

						throw new Exception(
								"Check-out is not succesful, please check with HRS administrator");
				}

			}

		}

	}

	/**
	 * This method is invoked when a customer wants to view the menu
	 * 
	 * @param connection
	 * @param description
	 * @throws SQLException
	 */
	public void createMenu(Connection connection, String description)
			throws SQLException

	{

		String newMenu = "insert ignore into Menu (description) VALUES (?)";
		PreparedStatement addMenu = connection.prepareStatement(newMenu,
				Statement.RETURN_GENERATED_KEYS);
		addMenu.setString(1, description);
		int addMenucount = addMenu.executeUpdate();

		if (addMenucount == 0)
			System.out.println("Food item already exists in the menu-list");

		else
		{
			ResultSet keys = addMenu.getGeneratedKeys();
			keys.next();
			int itemId = keys.getInt(1);
			System.out.println("The item id for the given menu-item is =>"
					+ itemId);
			DBTablePrinter.printTable(connection, "Menu");
			keys.close();
		}

		addMenu.close();

	}

	/**
	 * This method is invoked when a customer wants to view the menu
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	public void searchMenu(Connection connection) throws Exception {
		String menuDetails = "select * from Menu m";
		PreparedStatement menuSearchObject = connection
				.prepareStatement(menuDetails);
		checkForWarnings(menuSearchObject.getWarnings(),
				"fetching the menu details");
		ResultSet rs = menuSearchObject.executeQuery();
		while (rs.next()) {
			checkForWarnings(menuSearchObject.getWarnings(),
					"setting the menu details");
			Menu menu = new Menu();
			menu.setId(rs.getInt(1));
			menu.setDescription(rs.getString(2));
			menuList.add(menu);

		}
		DBTablePrinter.printTable(connection, "Menu");
		rs.close();

	}

	/**
	 * This method is invoked whenever the customer orders for food
	 * 
	 * @param connection
	 * @param custid
	 * @param order
	 * @throws Exception
	 */
	public void OrderedFood(Connection connection, int custId,
			Map<String, Integer> order) throws Exception {

		PreparedStatement validId = connection
				.prepareStatement("select c.id from Customer c "
						+ "where c.id =?");
		validId.setInt(1, custId);
		checkForWarnings(validId.getWarnings(), "fetching the customer-id");
		ResultSet custid = validId.executeQuery();
		if (!custid.first())
		{
			throw new SQLException("There is no customer with this id => "
					+ custId + "  " + "in the HRS");

		}
		else
		{

			String updateOrder = "insert into Foodorder(orderedBy) values(?)";
			PreparedStatement updateOrderObject = connection.prepareStatement(
					updateOrder, Statement.RETURN_GENERATED_KEYS);
			updateOrderObject.setInt(1, custId);
			updateOrderObject.executeUpdate();
			ResultSet keys = updateOrderObject.getGeneratedKeys();
			keys.next();
			int OrderId = keys.getInt(1);
			System.out.println("The order id is =>" + OrderId);

			String updateOrderDetails = "insert into FoodOrderDetails(description,quantity,foodOrder) values(?,?,?)";
			PreparedStatement orderFoodObject = connection
					.prepareStatement(updateOrderDetails);
			for (String key : order.keySet()) {
				orderFoodObject.setString(1, key);
				orderFoodObject.setInt(2, order.get(key));
				orderFoodObject.setInt(3, OrderId);
				orderFoodObject.executeUpdate();

			}
			System.out.println("Order updated.");
			DBTablePrinter.printTable(connection, "FoodOrder");
			DBTablePrinter.printTable(connection, "FoodOrderDetails");
			orderFoodObject.close();

		}
	}

	/**
	 * This is the main method from which all functionalities of HRS are invoked
	 * 
	 * @param args
	 * @throws Exception
	 */

	public static void main(String args[]) throws Exception {

		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager
					.getConnection(
							"jdbc:mysql://127.0.0.1:3306/testdb?autoReconnect=true&useSSL=false",
							"root", "test@1234");
		} catch (SQLException e) {
			System.err.println("Unable to connect to the database due to " + e);
		}
		HRS hrs = new HRS();

		/**
		 * Customer related Functionality
		 */

		//
		//hrs.bookRoom(connection, 10, Date.valueOf("2017-05-23"),
		//Date.valueOf("2017-05-24"), "deluxe", 1200);
		//hrs.searchRoom(connection, Date.valueOf("2017-05-13"),
		// Date.valueOf("2017-05-14"), "deluxe");
		//hrs.bookingDetails(connection, 10, 19);
		//hrs.cancelBooking(connection, 10, 16);
		// hrs.checkIn(connection, 10, 19);
		// hrs.searchMenu(connection);
		 Map<String, Integer> temp = new HashMap<String, Integer>();
		 temp.put("bacon123", 2);
		 temp.put("bread", 3);
		 hrs.OrderedFood(connection, 10, temp);

		/**
		 * HRS admin functionality
		 */
		//hrs.addCustomer(connection, "user1", "user1@yahoo.com", 24);
		//hrs.addRoom(connection, "deluxe", 1500);
		//hrs.searchallCustomer(connection);
		 //hrs.allRooms(connection);
		 //hrs.deleteRoom(connection,7);
		//hrs.allBookingDetails(connection);
		 //hrs.checkOut(connection, 10, 19);
		//hrs.createMenu(connection,"NewFood");

	}
}
