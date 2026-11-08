package com.railway.booking.backend.service;

import com.railway.booking.backend.dao.TrainSeatDAO;
import com.railway.booking.backend.model.TrainSeat;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for train seat configuration management
 */
@Service
public class TrainSeatService {
    private final TrainSeatDAO trainSeatDAO;

    public TrainSeatService(TrainSeatDAO trainSeatDAO) {
        this.trainSeatDAO = trainSeatDAO;
    }

    public List<TrainSeat> getSeatsByTrainId(int trainId) {
        return trainSeatDAO.getSeatsByTrainId(trainId);
    }

    public TrainSeat getSeatByTrainAndType(int trainId, TrainSeat.CoachType coachType) {
        return trainSeatDAO.getSeatByTrainAndType(trainId, coachType);
    }

    public boolean createTrainSeat(TrainSeat trainSeat) {
        return trainSeatDAO.createTrainSeat(trainSeat);
    }

    public boolean updateTrainSeat(TrainSeat trainSeat) {
        return trainSeatDAO.updateTrainSeat(trainSeat);
    }

    public boolean deleteTrainSeat(int trainSeatId) {
        return trainSeatDAO.deleteTrainSeat(trainSeatId);
    }

    public double getFareMultiplier(int trainId, TrainSeat.CoachType coachType) {
        return trainSeatDAO.getFareMultiplier(trainId, coachType);
    }
}
