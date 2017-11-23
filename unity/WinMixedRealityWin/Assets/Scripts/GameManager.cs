﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GameManager : MonoBehaviour {

    public GameObject BasicModel;
    public GameObject FootballModel;
    public GameObject FunctionModel;
    public GameObject KleinModel;
    public GameObject LissajousModel;
    public GameObject PenroseModel;
    public GameObject RuledSurfaceModel;
    public GameObject SierpinskiModel;
    public GameObject TempModel;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

    public void OnButtonPressed ()
    {
        //Destroy all GameObjects with tag "Model"
        GameObject[] gameObjects;  
        gameObjects = GameObject.FindGameObjectsWithTag("Model");
        for (var i = 0; i < gameObjects.Length; i++)
        {
            Destroy(gameObjects[i]);
        }

        Debug.Log("In OnButtonPressed");
        // Create GameObject
        Instantiate(TempModel, new Vector3(0, 0, 0), Quaternion.identity);
    }

}