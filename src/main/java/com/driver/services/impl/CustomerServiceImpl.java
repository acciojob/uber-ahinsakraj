package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		Customer savedCustomer = customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Optional<Customer> optionalCustomer = customerRepository2.findById(customerId);
//		if(optionalCustomer.isPresent()) {
//			Customer customer = optionalCustomer.get();
//			customerRepository2.delete(customer);
//		}
		optionalCustomer.ifPresent(customer -> customerRepository2.delete(customer));


	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		// Get the Driver
		Driver driver = null;

		int minDriverId = Integer.MAX_VALUE;
		List<Driver> driverList = driverRepository2.findAll();
		Boolean driverIsAvailable = false;
		for(Driver d : driverList) {
			if(d.getCab().getAvailable() && d.getDriverId() < minDriverId) {
				driver = d;
				minDriverId = d.getDriverId();
				driverIsAvailable = true;
			}
		}

		if(!driverIsAvailable) {
            throw new Exception("No cab available!");
        }

		// Verify Customer
		Optional<Customer> optionalCustomer = customerRepository2.findById(customerId);
		if(!optionalCustomer.isPresent()) {
			return null;
		}
		Customer customer = optionalCustomer.get();

		// Prepare tripBooking
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);

		int bill = distanceInKm * driver.getCab().getPerKmRate();
		tripBooking.setBill(bill);

//		tripBooking.setCustomer(customer);
//		tripBooking.setDriver(driver);

		TripBooking savedTrip = tripBookingRepository2.save(tripBooking);



		customer.getTripBookingList().add(savedTrip);
		driver.getTripBookingList().add(savedTrip);

		customerRepository2.save(customer);
		driverRepository2.save(driver);

		return savedTrip;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		if(optionalTripBooking.isPresent()) {
			TripBooking tripBooking = optionalTripBooking.get();
			tripBooking.setTripStatus(TripStatus.CANCELED);
			tripBookingRepository2.save(tripBooking);
		}
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		if(optionalTripBooking.isPresent()) {
			TripBooking tripBooking = optionalTripBooking.get();
			tripBooking.setTripStatus(TripStatus.COMPLETED);
			tripBookingRepository2.save(tripBooking);
		}
	}
}
