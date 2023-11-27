package com.cg.transaction.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cg.transaction.dao.TransactionDao;
import com.cg.transaction.dao.UserDao;
import com.cg.transaction.dao.WalletDao;
import com.cg.transaction.entity.Transaction;
import com.cg.transaction.repository.TransactionRepositroy;

@Service
public class TransactionServiceImpl implements ITransactionService {

	@Autowired
	private TransactionRepositroy transactionRepositroy;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public String TransferMoney(Transaction transactionDao) {

		int userId = transactionDao.getUserId();
		int reward=0;
		double creditAmount =0;
		Transaction transaction=new Transaction();
        //check user is register or not 
		UserDao userDao = restTemplate.getForObject("http://USER-MODULE/user/getuserbyid/" + userId, UserDao.class);
		
		//check senderWalletId is present or not 
		WalletDao senderWallet = restTemplate.getForObject(
				"http://WALLET-MODULE/wallet/getWalletById/" + transactionDao.getSenderWalletId(), WalletDao.class);
		
//check reciverWalletId present or not 
		
		WalletDao reciverWallet = restTemplate.getForObject(
		"http://WALLET-MODULE/wallet/getWalletById/" + transactionDao.getReceiverWalletId(),
		WalletDao.class);

		if (userDao == null)
		{
			return "Enter Valid UserId ";

		} 
			
		else if ( senderWallet.getUserId() != userId )
		{
			return "Enter Your WalletId ";

		} 
			
		else if ( reciverWallet == null )
		{
			return "Enter Correct Reciver Wallet Id ";

		} 
			
			
		
					 

						else {
									double amount = senderWallet.getBalance();
									if (transactionDao.getAmount() > amount) 
									{
									return "You dont have sufficient Balance for transaction";
									}

									else 
									{
					
										double debitAmount = transactionDao.getAmount();
										    creditAmount = amount - debitAmount;
											if (debitAmount < 250) {
											reward=3;
											creditAmount+=reward;}
											
											else if(debitAmount>=250 && debitAmount <1000)
											{ 
												reward=10;
					
												creditAmount+=reward;
											}
											else if(debitAmount>=1000 && debitAmount <1500)
											{ 
												reward=12;
												creditAmount+=reward;
											}
											else if(debitAmount>=1500 && debitAmount <2000)
											{ 
												reward=15;
												creditAmount+=reward;
											}
											else
											{
												reward=20;
												creditAmount+=reward;
											}
											
											
							transaction.setAmount(transactionDao.getAmount());
							transaction.setReceiverWalletId(transactionDao.getReceiverWalletId());
							transaction.setRewardAmount(reward);
							transaction.setSenderWalletId(transactionDao.getSenderWalletId());
							transaction.setStatus("Succesfull");
							
							transaction.setUserId(transactionDao.getUserId());
							transactionRepositroy.save(transaction);
						
						//update into Sender Account
						WalletDao senderwallet=new WalletDao();
						senderwallet.setUserId(transactionDao.getUserId());
						senderwallet.setBalance(creditAmount);
						 restTemplate.put(
						        "http://WALLET-MODULE/wallet/updateAmount",  // Update the URL accordingly
						        senderwallet,  // Pass the WalletDao object as the request body
						        String.class);
							 //update into receiver Account 
							 
							 WalletDao reciverWalletAmount =new WalletDao();
						double reciverAccountAmount= reciverWallet.getBalance();
						double reciverCreditAmount=reciverAccountAmount+transactionDao.getAmount();
						 reciverWalletAmount.setUserId(reciverWallet.getUserId());
						 reciverWalletAmount.setBalance(reciverCreditAmount);
						 
					 restTemplate.put("http://WALLET-MODULE/wallet/updateAmount",reciverWalletAmount,String.class);

											
			 return "Transaction get Succesfully !! you Have Recived Reward of " 
			 +reward +" Rs " +"And Your Current Balance is "+creditAmount;
									}
									
									
				
				
				
							} 

				
		
		
	
	}
	@Override
	public List<Transaction> getAllTransaction(int userId, int pageSize, int pageNumber) {
	    Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    Page<Transaction> pageTransaction = transactionRepositroy.findByUserId(userId, pageable);
	    List<Transaction> transactionList = pageTransaction.getContent();
	    
	    return transactionList;
	}
	
	
	@Override
	public List<Transaction> getAllTransactionById(int userId)
	{
		
		Optional<List<Transaction>> optionalListTransactions=transactionRepositroy.findByUserId(userId);
		if(optionalListTransactions.isEmpty())
		{
			return null;
		}
		List<Transaction> transactionList =optionalListTransactions.get();
		
		 return transactionList;
	}
	


}
