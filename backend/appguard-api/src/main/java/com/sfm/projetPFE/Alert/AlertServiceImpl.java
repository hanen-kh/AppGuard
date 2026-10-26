package com.sfm.projetPFE.Alert;

import com.sfm.projetPFE.Config.EmailService;
import com.sfm.projetPFE.Role.TypeRole;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.User.User;
import com.sfm.projetPFE.User.UserRepository;
import io.lettuce.core.models.role.RedisInstance;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
public class AlertServiceImpl implements AlertService {
    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;



    @Autowired
    private SimpMessagingTemplate messagingTemplate;



@Override
    public Alert triggerAlert(String type, String message) {
        Alert alert = new Alert();
        alert.setType(type);
        alert.setMessage(message);
        alert.setStatus(AlertStatus.UNRESOLVED);
        alert.setDate(LocalDateTime.now());
      return  alertRepository.save(alert);


    }

/*@Override
    public void notifyAdminsAndSupervisors(Server server, Alert alert) {
        Set<User> supervisors = server.getClient().getSupervisors();
        List<User> admins = userRepository.findByRole_Type(TypeRole.ADMIN);

        Set<User> recipients = new HashSet<>(supervisors);
        recipients.addAll(admins);

        for (User user : recipients) {
            messagingTemplate.convertAndSend("/topic/admin/alerts/" + user.getId(), alert);
        }
    }*/




}
