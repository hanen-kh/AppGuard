package com.sfm.projetPFE;

import com.sfm.projetPFE.Client.*;
import com.sfm.projetPFE.CronJob.Cron.CronJobDtos;
import com.sfm.projetPFE.CronJob.Cron.CronJobRepository;
import com.sfm.projetPFE.CronJob.Cron.CronJobServiceImpl;
import com.sfm.projetPFE.Role.Role;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.User.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.times;

@SpringBootTest
class ProjetPfeApplicationTests {

	@InjectMocks
	private UserServiceImpl userService;


	@InjectMocks
	private CronJobServiceImpl cronJobService;

	@Mock
	private CronJobRepository cronJobRepository;

	@InjectMocks
	private ClientServiceImpl clientService; // Votre service réel

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testCreateClient() {
		ClientDTO dto = new ClientDTO();
		dto.setName("Client1");
		dto.setContactEmail("client1@test.com");
		dto.setDescription("Description client1");

		when(clientRepository.findByName("Client1")).thenReturn(Optional.empty());

		assertDoesNotThrow(() -> clientService.createClient(dto));


	}


	@Test
	void testUpdateClient() {
		Client existingClient = new Client();
		existingClient.setId(1L);
		existingClient.setName("OldName");

		ClientDTO dto = new ClientDTO();
		dto.setName("NewName");
		dto.setContactEmail("new@test.com");
		dto.setDescription("Updated description");

		when(clientRepository.findById(1)).thenReturn(Optional.of(existingClient));
		when(clientRepository.findByName("NewName")).thenReturn(Optional.empty());


		assertDoesNotThrow(() -> clientService.updateClient(1L, dto));


	}

	@Test
	void testCreateUser_Success() {
		UserDTO dto = new UserDTO();
		dto.setFirstname("John");
		dto.setLastname("Doe");
		dto.setEmail("john.doe@test.com");
		dto.setRoleId(1);

		when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.empty());


	}

	@Test
	void testDeleteUser_Success() {
		User user = new User();
		user.setId(1);

		when(userRepository.findById(1)).thenReturn(Optional.of(user));

		assertDoesNotThrow(() -> userService.deleteUser(1));}

	@Test
	void testCreateCronJob_Success() throws Exception {
		Long serverId = 1L;
		Server server = new Server();
		server.setId(serverId.intValue());
		server.setAgentUrl("http://localhost:8080/agent");
		server.setAgentApiKey("apikey123");

		CronJobDtos.CreateCronRequest request = new CronJobDtos.CreateCronRequest();
		request.setName("Job1");
		request.setCommand("echo test");
		request.setCronExpression("0 0 * * * *");
		request.setDescription("Test cron");
		request.setTimeoutMinutes(5);
		request.setRetryCount(2);




	}




}
